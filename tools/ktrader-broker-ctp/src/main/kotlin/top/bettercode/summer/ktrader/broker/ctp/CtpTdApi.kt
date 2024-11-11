@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package top.bettercode.summer.ktrader.broker.ctp

import kotlinx.coroutines.*
import org.rationalityfrontline.jctp.*
import org.rationalityfrontline.jctp.jctpConstants.*
import org.rationalityfrontline.kevent.KEvent
import top.bettercode.summer.ktrader.broker.api.*
import top.bettercode.summer.ktrader.broker.ctp.Converter.directionC2A
import top.bettercode.summer.ktrader.datatype.*
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CtpTdApi(config: CtpConfig, kEvent: KEvent, sourceId: String) :
    CtpApi(config, kEvent, sourceId) {
    private val tdApi: CThostFtdcTraderApi
    private val tdSpi: CtpTdSpi

    /**
     * 本地维护的订单引用（同一交易日内递增不重复的整数，做本地文件维护是因为 tdSpi.OnRtnTrade 的回报中只有 orderRef 而没有 frontId 与 sessionId，所以需要尽量保证 orderRef 的唯一性）。
     * 默认从 10000 开始（为了减少与其它 session 的订单引用重复的概率，更好的确保 orderRef 的跨 session 唯一性）
     */
    private val orderRef = AtomicInteger(10000)
    private fun nextOrderRef(): Int {
        val nextOrderRef = orderRef.incrementAndGet()
        cacheFile.writeText("$tradingDay\n$nextOrderRef")
        return nextOrderRef
    }

    /**
     * 协程 scope
     */
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * 用于记录维护交易日及 orderRef 的缓存文件
     */
    private val cacheFile: File

    /**
     * 行情 Api 对象，用于获取最新 Tick，并在查询全市场合约时更新其 codeMap
     */
    lateinit var mdApi: CtpMdApi

    /**
     * 前置编号（客户端连接到的前置机的编号，同一交易前置地址的前置编号相同），用于确定 orderId，参见 tdSpi.OnRspUserLogin
     */
    var frontId: Int = 0
        private set

    /**
     * 会话编号（客户端连接到前置机的连接会话编号，不同连接的会话编号不同，断开重连后也会变动），用于确定 orderId，参见 tdSpi.OnRspUserLogin
     */
    var sessionId: Int = 0
        private set

    /**
     * 期货保证金类型
     */
    private var futuresMarginPriceType: MarginPriceType = MarginPriceType.PRE_SETTLEMENT_PRICE

    /**
     * 期权保证金类型
     */
    private var optionsMarginPriceType: MarginPriceType = MarginPriceType.PRE_SETTLEMENT_PRICE

    /**
     * 上次查询账户资产的时间
     */
    private var lastQueryAssetsTime = 0L

    init {
        val cachePath = config.cachePath.ifBlank { "./data/ctp/" }
        val tdCachePath =
            "${if (cachePath.endsWith('/')) cachePath else "$cachePath/"}${config.investorId.ifBlank { "unknown" }}/td/"
        File(tdCachePath).mkdirs()
        cacheFile = File("${tdCachePath}cache.txt")
        tdApi = CThostFtdcTraderApi.CreateFtdcTraderApi(tdCachePath)
        tdSpi = CtpTdSpi()
        tdApi.apply {
            RegisterSpi(tdSpi)
            // QUICK 订阅私有流
            SubscribePrivateTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK)
            // RESTART 订阅公有流，用于获取合约交易状态，参见 tdSpi.OnRtnInstrumentStatus
            SubscribePublicTopic(THOST_TE_RESUME_TYPE.THOST_TERT_RESTART)
            config.tdFronts.forEach { tFront ->
                RegisterFront(tFront)
            }
        }
    }

    /**
     * 连接交易前置并自动完成登录（还会自动查询持仓、订单、成交记录等信息，详见 [CtpTdSpi.OnFrontConnected]）。在无法连接至前置的情况下可能会长久阻塞。
     * 该操作不可加超时限制，因为可能在双休日等非交易时间段启动程序。
     */
    @JvmOverloads
    suspend fun connect(timeoutMills: Long? = null) {
        if (inited) return
        val block: (Continuation<Unit>) -> Unit = { continuation ->
            val requestId =
                Int.MIN_VALUE // 因为 OnFrontConnected 中 requestId 会重置为 0，为防止 requestId 重复，取整数最小值
            requestMap[requestId] = RequestContinuation(requestId, continuation, "connect")
            postBrokerLogEvent(LogLevel.INFO, "【交易接口】$requestId 连接前置服务器...")
            tdApi.Init()
            inited = true
        }
        if (timeoutMills != null) {
            suspendCoroutineWithTimeout("connect", timeoutMills, block)
        } else {
            suspendCoroutine(block)
        }
    }

    /**
     * 关闭并释放资源，会发送一条 [BrokerEventType.CONNECTION] ([ConnectionEventType.TD_NET_DISCONNECTED]) 信息
     */
    fun close() {
        if (frontConnected) tdSpi.OnFrontDisconnected(0)
        scope.cancel()

        tdApi.Release()
        tdApi.delete()
    }

    /**
     * 获取当前交易日
     */
    fun tradingDay(): String {
        return if (connected) tradingDay else tdApi.GetTradingDay()
    }

    /**
     * 查询最新 [Tick]
     */
    @JvmOverloads
    suspend fun queryLastTick(
        code: String,
        useCache: Boolean = true
    ): DepthMarketData? {
        if (useCache) {
            val cachedTick = mdApi.lastTicks[code]
            if (cachedTick != null) {
                return cachedTick
            }
        }
        val qryField = CThostFtdcQryDepthMarketDataField().apply {
            instrumentID = parseCode(code).second
        }
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 查询最新行情：$code ...")
            tdApi.ReqQryDepthMarketData(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryLastTick", TIMEOUT_MILLS) { continuation ->
                requestMap[requestId] = RequestContinuation(requestId, continuation, data = code)
            }
        })
    }

    /**
     * 查询某一特定合约的信息。[extras.queryFee: Boolean = false]【是否查询保证金率及手续费率，如果之前没查过，可能会耗时。当 useCache 为 false 时无效】
     */
    suspend fun queryInstrument(
        code: String
    ): Instrument? {
        val (exchangeId, instrumentId) = parseCode(code)
        val qryField = CThostFtdcQryInstrumentField().apply {
            exchangeID = exchangeId
            instrumentID = instrumentId
        }
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 查询合约信息：$code ...")
            tdApi.ReqQryInstrument(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryInstrument", TIMEOUT_MILLS) { continuation ->
                requestMap[requestId] = RequestContinuation(requestId, continuation, data = code)
            }
        })
    }

    /**
     * 查询全市场合约的信息
     */
    suspend fun queryAllInstruments(timeoutMills: Long = TIMEOUT_MILLS * 2): List<Instrument>? {
        val qryField = CThostFtdcQryInstrumentField()
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 查询全市场合约信息...")
            tdApi.ReqQryInstrument(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryAllInstruments", timeoutMills) { continuation ->
                requestMap[requestId] = RequestContinuation(
                    requestId,
                    continuation,
                    data = mutableListOf<Instrument>()
                )
            }
        })
    }

    suspend fun queryProduct(
        productId: String
    ): Product? {
        val qryField = CThostFtdcQryProductField().apply {
            productID = productId
        }
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 查询产品信息：$productId ...")
            tdApi.ReqQryProduct(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryProduct", TIMEOUT_MILLS) { continuation ->
                requestMap[requestId] =
                    RequestContinuation(requestId, continuation, data = productId)
            }
        })
    }

    /**
     * 查询全市场产品的信息
     */
    suspend fun queryAllProducts(timeoutMills: Long = TIMEOUT_MILLS * 2): List<Product>? {
        val qryField = CThostFtdcQryProductField()
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 查询全市场产品信息...")
            tdApi.ReqQryProduct(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryAllProducts", timeoutMills) { continuation ->
                requestMap[requestId] = RequestContinuation(
                    requestId,
                    continuation,
                    data = mutableListOf<Product>()
                )
            }
        })
    }

    /**
     * 查询成交记录
     */
    @JvmOverloads
    suspend fun queryTrades(
        code: String? = null,
    ): List<Trade>? {
        val reqData = QueryTradesData()
        val qryField = CThostFtdcQryTradeField().apply {
            brokerID = config.brokerId
            investorID = config.investorId
            if (code != null) {
                val (excId, insId) = parseCode(code)
                exchangeID = excId
                instrumentID = insId
                reqData.code = code
            }
        }
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(
                LogLevel.TRACE,
                "【交易接口】$requestId 查询[${code ?: "所有"}]成交记录..."
            )
            tdApi.ReqQryTrade(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryTrades", TIMEOUT_MILLS * 5) { continuation ->
                requestMap[requestId] =
                    RequestContinuation(requestId, continuation, data = reqData)
            }
        })
    }

    /**
     * 查询账户资金信息
     */
    suspend fun queryAssets(): TradingAccount? {
        val qryField = CThostFtdcQryTradingAccountField().apply {
            brokerID = config.brokerId
            investorID = config.investorId
            currencyID = "CNY"
        }
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 查询账户资金信息...")
            tdApi.ReqQryTradingAccount(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryAssets", TIMEOUT_MILLS) { continuation ->
                requestMap[requestId] = RequestContinuation(requestId, continuation)
            }
        })
    }

    /**
     * 查询合约 [code] 的 [direction] 方向的持仓信息，如果 [code] 为 null（默认），则查询账户整体持仓信息
     */
    @JvmOverloads
    suspend fun queryPositions(
        code: String? = null,
        direction: Direction? = null
    ): List<InvestorPosition>? {
        if (direction == Direction.UNKNOWN) return emptyList()
        val qryField = CThostFtdcQryInvestorPositionField().apply {
            if (!code.isNullOrEmpty()) instrumentID = parseCode(code).second
        }
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(
                LogLevel.TRACE,
                "【交易接口】$requestId 查询[${code ?: "所有"}]持仓信息..."
            )
            tdApi.ReqQryInvestorPosition(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryPositions", TIMEOUT_MILLS) { continuation ->
                requestMap[requestId] = RequestContinuation(
                    requestId,
                    continuation,
                    tag = direction?.name ?: code ?: "",
                    data = mutableListOf<InvestorPosition>()
                )
            }
        })
    }

    /**
     * 查询合约 [code] 的 [direction] 方向的持仓明细，如果 [code] 为 null（默认），则查询账户整体持仓明细
     */
    @JvmOverloads
    suspend fun queryPositionDetails(
        code: String? = null,
        direction: Direction? = null,
    ): List<InvestorPositionDetail>? {
        val qryField = CThostFtdcQryInvestorPositionDetailField().apply {
            brokerID = config.brokerId
            investorID = config.investorId
            if (!code.isNullOrBlank())
                instrumentID = parseCode(code).second
        }
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(
                LogLevel.TRACE,
                "【交易接口】$requestId 查询[${code ?: "所有"}]持仓明细..."
            )
            tdApi.ReqQryInvestorPositionDetail(qryField, requestId)
        }, {
            suspendCoroutineWithTimeout("queryPositionDetails", TIMEOUT_MILLS) { continuation ->
                requestMap[requestId] = RequestContinuation(
                    requestId,
                    continuation,
                    data = QueryPositionDetailsData(code, direction)
                )
            }
        })
    }

    /**
     * 查询组合持仓明细，如果 [code] 为 null（默认），则查询账户整体组合持仓明细
     */
    suspend fun queryPositionCombineDetails(
        code: String? = null,
    ): List<InvestorPositionCombineDetail>? {
        val qryField = CThostFtdcQryInvestorPositionCombineDetailField().apply {
            brokerID = config.brokerId
            investorID = config.investorId
        }
        val requestId = nextRequestId()
        return runWithResultCheck({
            postBrokerLogEvent(
                LogLevel.TRACE,
                "【交易接口】$requestId 查询[${code ?: "所有"}]组合持仓明细..."
            )
            tdApi.ReqQryInvestorPositionCombineDetail(
                qryField,
                requestId
            )
        }, {
            suspendCoroutineWithTimeout(
                "queryPositionCombineDetails",
                TIMEOUT_MILLS
            ) { continuation ->
                requestMap[requestId] = RequestContinuation(
                    requestId,
                    continuation,
                    data = QueryPositionCombineDetailsData(code)
                )
            }
        })
    }

    /**
     * 查询期货手续费率，如果 [code] 为 null（默认），则查询所有当前持仓合约的手续费率。如果遇到中金所合约，会进行申报手续费的二次查询。
     * 已查过手续费的不会再次查询。查询到的结果会自动更新到对应的 [instruments] 中
     */
    suspend fun queryFuturesCommissionRate(code: String? = null): List<InstrumentCommissionRate>? {
        return if (code == null) {
            val qryField = CThostFtdcQryInstrumentCommissionRateField().apply {
                brokerID = config.brokerId
                investorID = config.investorId
            }
            val requestId = nextRequestId()
            runWithResultCheck({
                postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 查询[所有]手续费率...")
                tdApi.ReqQryInstrumentCommissionRate(
                    qryField,
                    requestId
                )
            }, {
                suspendCoroutineWithTimeout(
                    "queryFuturesCommissionRate",
                    TIMEOUT_MILLS
                ) { continuation ->
                    requestMap[requestId] =
                        RequestContinuation(
                            requestId,
                            continuation,
                            data = mutableListOf<InstrumentCommissionRate>()
                        )
                }
            })
        } else {
            val qryField = CThostFtdcQryInstrumentCommissionRateField().apply {
                brokerID = config.brokerId
                investorID = config.investorId
                val (excId, insId) = parseCode(code)
                exchangeID = excId
                instrumentID = insId
            }
            val requestId = nextRequestId()
            runWithResultCheck({
                postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 查询[${code}]手续费率...")
                tdApi.ReqQryInstrumentCommissionRate(
                    qryField,
                    requestId
                )
            }, {
                suspendCoroutineWithTimeout(
                    "queryFuturesCommissionRate",
                    TIMEOUT_MILLS
                ) { continuation ->
                    requestMap[requestId] = RequestContinuation(
                        requestId,
                        continuation,
                        data = mutableListOf<CThostFtdcInstrumentCommissionRateField>()
                    )
                }
            })
        }
    }

    /**
     * Ctp TdApi 的回调类
     */
    private inner class CtpTdSpi : CThostFtdcTraderSpi() {

        /**
         * 请求客户端认证
         */
        private suspend fun reqAuthenticate() {
            val reqField = CThostFtdcReqAuthenticateField().apply {
                appID = config.appId
                authCode = config.authCode
                userProductInfo = config.userProductInfo
                userID = config.investorId
                brokerID = config.brokerId
            }
            val requestId = nextRequestId()
            runWithResultCheck<Unit?>({
                postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 请求客户端认证...")
                tdApi.ReqAuthenticate(reqField, requestId)
            }, {
                suspendCoroutineWithTimeout("reqAuthenticate", TIMEOUT_MILLS) { continuation ->
                    requestMap[requestId] = RequestContinuation(requestId, continuation)
                }
            })
        }

        /**
         * 请求用户登录
         */
        private suspend fun reqUserLogin() {
            val reqField = CThostFtdcReqUserLoginField().apply {
                userID = config.investorId
                password = config.password
                brokerID = config.brokerId
                userProductInfo = config.userProductInfo
            }
            val requestId = nextRequestId()
            runWithResultCheck<Unit?>({
                postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 请求用户登录...")
                tdApi.ReqUserLogin(reqField, requestId)
            }, {
                suspendCoroutineWithTimeout("reqUserLogin", TIMEOUT_MILLS) { continuation ->
                    requestMap[requestId] = RequestContinuation(requestId, continuation)
                }
            })
        }

        /**
         * 请求结算单确认
         */
        private suspend fun reqSettlementInfoConfirm() {
            val reqField = CThostFtdcSettlementInfoConfirmField().apply {
                investorID = config.investorId
                brokerID = config.brokerId
            }
            val requestId = nextRequestId()
            runWithResultCheck<Unit?>({
                postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$requestId 请求结算单确认...")
                tdApi.ReqSettlementInfoConfirm(reqField, requestId)
            }, {
                suspendCoroutineWithTimeout(
                    "reqSettlementInfoConfirm",
                    TIMEOUT_MILLS
                ) { continuation ->
                    requestMap[requestId] = RequestContinuation(requestId, continuation)
                }
            })
        }

        /**
         * 发生错误时回调。如果没有对应的协程请求，会发送一条 [BrokerEventType.LOG] 信息；有对应的协程请求时，会将其异常完成
         */
        override fun OnRspError(
            pRspInfo: CThostFtdcRspInfoField,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID]
            if (request == null) {
                val errorInfo = "${pRspInfo.errorMsg}, requestId=$nRequestID, isLast=$bIsLast"
                val connectRequests = requestMap.values.filter { it.tag == "connect" }
                if (connectRequests.isEmpty()) {
                    postBrokerLogEvent(LogLevel.ERROR, "【CtpTdSpi.OnRspError】$errorInfo")
                } else {
                    resumeRequestsWithException("connect", errorInfo)
                }
            } else {
                request.continuation.resumeWithException(Exception(pRspInfo.errorMsg))
                requestMap.remove(nRequestID)
            }
        }

        /**
         * 行情前置连接时回调。会将 [requestId] 置为 0；发送一条 [BrokerEventType.CONNECTION] 信息
         */
        override fun OnFrontConnected() {
            frontConnected = true
            requestId.set(0)
            postBrokerConnectionEvent(ConnectionEventType.TD_NET_CONNECTED)
            scope.launch {
                fun resumeConnectWithException(errorInfo: String) {
                    resumeRequestsWithException("connect", errorInfo)
                    throw CtpException(errorInfo)
                }
                try {
                    // 请求客户端认证
                    try {
                        reqAuthenticate()
                    } catch (e: Exception) {
                        resumeConnectWithException("请求客户端认证失败：$e")
                    }
                    // 请求用户登录
                    try {
                        reqUserLogin()
                    } catch (e: Exception) {
                        resumeConnectWithException("请求用户登录失败：$e")
                    }

                    // 登录操作完成
                    connected = true
                    postBrokerConnectionEvent(ConnectionEventType.TD_LOGGED_IN)
                    resumeRequests("connect", Unit)
                } catch (e: Exception) {  // 登录操作失败
                    resumeRequestsWithException("connect", e.message ?: e.toString())
                }
            }
        }

        /**
         * 交易前置断开连接时回调。会将 [connected] 置为 false；发送一条 [BrokerEventType.CONNECTION] 信息；异常完成所有的协程请求
         */
        override fun OnFrontDisconnected(nReason: Int) {
            frontConnected = false
            connected = false
            postBrokerConnectionEvent(
                ConnectionEventType.TD_NET_DISCONNECTED,
                "${getDisconnectReason(nReason)} ($nReason)"
            )
            val e = CtpException("网络连接断开：${getDisconnectReason(nReason)} ($nReason)")
            requestMap.values.forEach {
                it.continuation.resumeWithException(e)
            }
            requestMap.clear()
        }

        /**
         * 客户端认证请求响应
         */
        override fun OnRspAuthenticate(
            pRspAuthenticateField: CThostFtdcRspAuthenticateField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                if (bIsLast) {
                    postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$nRequestID 客户端认证成功")
                    (request.continuation as Continuation<Unit>).resume(Unit)
                    requestMap.remove(nRequestID)
                }
            }) { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            }
        }

        /**
         * 用户登录请求响应
         */
        override fun OnRspUserLogin(
            pRspUserLogin: CThostFtdcRspUserLoginField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                if (pRspUserLogin == null) {
                    request.continuation.resumeWithException(Exception("pRspUserLogin 为 null"))
                    requestMap.remove(nRequestID)
                    return
                }
                frontId = pRspUserLogin.frontID
                sessionId = pRspUserLogin.sessionID
                tradingDay = pRspUserLogin.tradingDay
                var lastTradingDay = ""
                var lastMaxOrderRef = 10000
                if (cacheFile.exists()) {
                    val lines = cacheFile.readLines()
                    if (lines.size >= 2) {
                        lastTradingDay = lines[0]
                        lastMaxOrderRef = lines[1].toIntOrNull() ?: lastMaxOrderRef
                    }
                }
                // 如果交易日未变，则延续使用上一次的 maxOrderRef
                if (lastTradingDay == tradingDay) {
                    orderRef.set(lastMaxOrderRef)
                } else { // 如果交易日变动，则清空各种缓存，并将 orderRef 重置为 10000
                    orderRef.set(10000)
                    cacheFile.writeText("$tradingDay\n${orderRef.get()}")
                    postBrokerEvent(BrokerEventType.NEW_TRADING_DAY, tradingDay)
                }
                if (bIsLast) {
                    postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$nRequestID 资金账户登录成功")
                    (request.continuation as Continuation<Unit>).resume(Unit)
                    requestMap.remove(nRequestID)
                }
            }) { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            }
        }

        /**
         * 结算单确认请求响应
         */
        override fun OnRspSettlementInfoConfirm(
            pSettlementInfoConfirm: CThostFtdcSettlementInfoConfirmField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                if (bIsLast) {
                    postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$nRequestID 结算单确认成功")
                    (request.continuation as Continuation<Unit>).resume(Unit)
                    requestMap.remove(nRequestID)
                }
            }) { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            }
        }

        /**
         * 成交记录查询请求响应
         */
        override fun OnRspQryTrade(
            pTrade: CThostFtdcTradeField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                val reqData = request.data as QueryTradesData
                if (pTrade != null) {
                    reqData.results.add(Trade.from(pTrade))
                }
                if (bIsLast) {
                    if (reqData.code != null) {
                        reqData.results.removeAll { "${it.exchangeID}.${it.instrumentID}" != reqData.code }
                    }
                    (request.continuation as Continuation<List<Trade>>).resume(
                        reqData.results
                    )
                    requestMap.remove(nRequestID)
                    postBrokerLogEvent(LogLevel.TRACE, "【交易接口】$nRequestID 查询成交记录完成")
                }
            }, { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            })
        }

        /**
         *  Tick 查询请求响应
         */
        override fun OnRspQryDepthMarketData(
            pDepthMarketData: CThostFtdcDepthMarketDataField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            val reqCode = request.data as String
            checkRspInfo(pRspInfo, {
                if (pDepthMarketData == null) {
                    (request.continuation as Continuation<DepthMarketData?>).resume(
                        null
                    )
                    requestMap.remove(nRequestID)
                    postBrokerLogEvent(
                        LogLevel.TRACE,
                        "【交易接口】$nRequestID 查询 Tick 失败：$reqCode"
                    )
                    return
                }
                val code = "${pDepthMarketData.exchangeID}.${pDepthMarketData.instrumentID}"
                if (code == reqCode) {
                    (request.continuation as Continuation<DepthMarketData?>).resume(
                        DepthMarketData.from(pDepthMarketData)
                    )
                    requestMap.remove(nRequestID)
                    postBrokerLogEvent(
                        LogLevel.TRACE,
                        "【交易接口】$nRequestID 查询 Tick 完成：$reqCode"
                    )
                } else {
                    if (bIsLast) {
                        (request.continuation as Continuation<DepthMarketData?>).resume(
                            null
                        )
                        requestMap.remove(nRequestID)
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询 Tick 失败：$reqCode"
                        )
                    }
                }
            }, { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            })
        }

        /**
         * 合约信息查询请求响应
         */
        override fun OnRspQryInstrument(
            pInstrument: CThostFtdcInstrumentField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            val reqData = request.data
            checkRspInfo(pRspInfo, {
                // 如果是查询单个合约
                if (reqData is String) {
                    val con = request.continuation as Continuation<Instrument?>
                    if (pInstrument == null) {
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询全市场合约:null"
                        )
                        con.resume(null)
                        requestMap.remove(nRequestID)
                        return
                    }
                    val code = "${pInstrument.exchangeID}.${pInstrument.instrumentID}"
                    if (reqData == code) {
                        con.resume(Instrument.from(pInstrument))
                        requestMap.remove(nRequestID)
                    } else {
                        if (bIsLast) {
                            postBrokerLogEvent(
                                LogLevel.TRACE,
                                "【交易接口】$nRequestID 查询全市场合约:null"
                            )
                            con.resume(null)
                            requestMap.remove(nRequestID)
                        }
                    }
                } else { // 如果是查询多个合约
                    val insList = request.data as MutableList<Instrument>
                    if (pInstrument != null) insList.add(Instrument.from(pInstrument))
                    if (bIsLast) {
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询全市场合约成功:${insList.size}"
                        )
                        (request.continuation as Continuation<List<Instrument>>).resume(
                            insList
                        )
                        requestMap.remove(nRequestID)
                    }
                }
            }, { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            })
        }

        /**
         * 合约信息查询请求响应
         */
        override fun OnRspQryProduct(
            pProduct: CThostFtdcProductField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            val reqData = request.data
            checkRspInfo(pRspInfo, {
                // 如果是查询单个合约
                if (reqData is String) {
                    val con = request.continuation as Continuation<Product?>
                    if (pProduct == null) {
                        con.resume(null)
                        requestMap.remove(nRequestID)
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询合约信息失败：$reqData"
                        )
                        return
                    }
                    if (reqData == pProduct.productID) {
                        con.resume(Product.from(pProduct))
                        requestMap.remove(nRequestID)
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询合约信息成功：$reqData"
                        )
                    } else {
                        if (bIsLast) {
                            con.resume(null)
                            requestMap.remove(nRequestID)
                            postBrokerLogEvent(
                                LogLevel.TRACE,
                                "【交易接口】$nRequestID 查询合约信息失败：$reqData"
                            )
                        }
                    }
                } else { // 如果是查询多个合约
                    val insList = request.data as MutableList<Product>
                    if (pProduct != null) insList.add(Product.from(pProduct))
                    if (bIsLast) {
                        (request.continuation as Continuation<List<Product>>).resume(
                            insList
                        )
                        requestMap.remove(nRequestID)
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询合约信息成功：${insList.size}"
                        )
                    }
                }
            }, { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            })
        }

        /**
         * 账户资金查询请求响应
         */
        override fun OnRspQryTradingAccount(
            pTradingAccount: CThostFtdcTradingAccountField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                if (pTradingAccount == null) {
                    request.continuation.resumeWithException(Exception("pTradingAccount 为 null"))
                    requestMap.remove(nRequestID)
                    return
                }
                (request.continuation as Continuation<TradingAccount>).resume(
                    TradingAccount.from(pTradingAccount)
                )
                requestMap.remove(nRequestID)
                postBrokerLogEvent(
                    LogLevel.TRACE,
                    "【交易接口】$nRequestID 查询账户资金成功"
                )
                lastQueryAssetsTime = System.currentTimeMillis()
            }, { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            })
        }

        /**
         * 账户持仓查询请求响应，会自动合并上期所的昨仓和今仓
         */
        override fun OnRspQryInvestorPosition(
            pInvestorPosition: CThostFtdcInvestorPositionField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                val posList = request.data as MutableList<InvestorPosition>
                if (pInvestorPosition != null) {
                    posList.add(InvestorPosition.from(pInvestorPosition))
                }
                if (bIsLast) {
                    when (request.tag) {
                        Direction.LONG.name -> {
                            (request.continuation as Continuation<InvestorPosition?>).resume(
                                posList.find { directionC2A(it.posiDirection) == Direction.LONG })
                            requestMap.remove(nRequestID)
                        }

                        Direction.SHORT.name -> {
                            (request.continuation as Continuation<InvestorPosition?>).resume(
                                posList.find { directionC2A(it.posiDirection) == Direction.SHORT })
                            requestMap.remove(nRequestID)
                        }

                        else -> {
                            (request.continuation as Continuation<List<InvestorPosition>>).resume(
                                posList
                            )
                            requestMap.remove(nRequestID)
                        }
                    }
                    postBrokerLogEvent(
                        LogLevel.TRACE,
                        "【交易接口】$nRequestID 查询持仓信息成功：${posList.size}"
                    )
                }
            }, { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            })
        }

        /**
         * 持仓明细查询请求响应
         */
        override fun OnRspQryInvestorPositionDetail(
            pInvestorPositionDetail: CThostFtdcInvestorPositionDetailField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                val reqData = request.data as QueryPositionDetailsData
                if (pInvestorPositionDetail != null) {
                    reqData.results.add(InvestorPositionDetail.from(pInvestorPositionDetail))
                }
                if (bIsLast) {
                    if (reqData.code == null || reqData.direction == null) {  // 查询多个
                        (request.continuation as Continuation<List<InvestorPositionDetail>>).resume(
                            reqData.results
                        )
                        requestMap.remove(nRequestID)
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询持仓明细信息成功：${reqData.results.size}"
                        )
                    } else {  // 查询单个
                        val details =
                            reqData.results.find {
                                "${it.exchangeID}.${it.instrumentID}" == reqData.code && directionC2A(
                                    it.direction
                                ) == reqData.direction
                            }
                        (request.continuation as Continuation<InvestorPositionDetail?>).resume(
                            details
                        )
                        requestMap.remove(nRequestID)
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询持仓明细信息成功：${details}"
                        )
                    }
                }
            }, { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            })
        }

        /**
         * 持仓明细查询请求响应
         */
        override fun OnRspQryInvestorPositionCombineDetail(
            pInvestorPositionCombineDetail: CThostFtdcInvestorPositionCombineDetailField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                val reqData = request.data as QueryPositionCombineDetailsData
                if (pInvestorPositionCombineDetail != null) {
                    reqData.results.add(
                        InvestorPositionCombineDetail.from(
                            pInvestorPositionCombineDetail
                        )
                    )
                }
                if (bIsLast) {
                    if (reqData.code == null || reqData.direction == null) {  // 查询多个
                        (request.continuation as Continuation<List<InvestorPositionCombineDetail>>).resume(
                            reqData.results
                        )
                        requestMap.remove(nRequestID)
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询持仓明细信息成功：${reqData.results.size}"
                        )
                    } else {  // 查询单个
                        val details =
                            reqData.results.find {
                                "${it.exchangeID}.${it.instrumentID}" == reqData.code && directionC2A(
                                    it.direction
                                ) == reqData.direction
                            }
                        (request.continuation as Continuation<InvestorPositionCombineDetail?>).resume(
                            details
                        )
                        requestMap.remove(nRequestID)
                        postBrokerLogEvent(
                            LogLevel.TRACE,
                            "【交易接口】$nRequestID 查询持仓明细信息成功：${details}"
                        )
                    }
                }
            }, { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            })
        }


        /**
         * 期货手续费率查询请求响应，会自动更新 [instruments] 中对应的手续费率信息
         */
        override fun OnRspQryInstrumentCommissionRate(
            pCommissionRate: CThostFtdcInstrumentCommissionRateField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            val request = requestMap[nRequestID] ?: return
            checkRspInfo(pRspInfo, {
                if (pCommissionRate != null) {
                    (request.data as MutableList<InstrumentCommissionRate>).add(
                        InstrumentCommissionRate.from(pCommissionRate)
                    )
                }
                if (bIsLast) {
                    (request.continuation as Continuation<List<InstrumentCommissionRate>>).resume(
                        request.data as MutableList<InstrumentCommissionRate>
                    )
                    requestMap.remove(nRequestID)
                    postBrokerLogEvent(
                        LogLevel.TRACE,
                        "【交易接口】$nRequestID 查询手续费率信息成功：${request.data.size}"
                    )
                }
            }) { errorCode, errorMsg ->
                request.continuation.resumeWithException(Exception("$errorMsg ($errorCode)"))
                requestMap.remove(nRequestID)
            }
        }
    }
}