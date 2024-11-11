@file:Suppress(
    "UNCHECKED_CAST",
    "MemberVisibilityCanBePrivate",
    "CanBeParameter"
)

package top.bettercode.summer.ktrader.broker.ctp

import kotlinx.coroutines.*
import org.rationalityfrontline.jctp.*
import org.rationalityfrontline.kevent.KEvent
import top.bettercode.summer.ktrader.broker.api.*
import top.bettercode.summer.ktrader.datatype.DepthMarketData
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

class CtpMdApi(config: CtpConfig, kEvent: KEvent, sourceId: String) :
    CtpApi(config, kEvent, sourceId) {
    private val mdApi: CThostFtdcMdApi
    private val mdSpi: CtpMdSpi

    /**
     * 交易 Api 对象，用于获取合约的乘数、状态
     */
    lateinit var tdApi: CtpTdApi

    /**
     * 当前交易日内已订阅的合约代码集合（当交易日发生更替时上一交易日的订阅会自动失效清零）
     */
    private val subscriptions: MutableSet<String> = mutableSetOf()

    /**
     * 缓存的合约代码列表，key 为 InstrumentID, value 为 ExchangeID.InstrumentID（因为 OnRtnDepthMarketData 返回的数据中没有 ExchangeID，所以需要在订阅时缓存完整代码，在 CtpTdApi 获取到全合约信息时会被填充）
     */
    private val codeMap = mutableMapOf<String, String>()

    /**
     * 缓存的 [CThostFtdcDepthMarketDataField] 表，key 为 code，value 为 [CThostFtdcDepthMarketDataField]。每当网络断开（OnFrontDisconnected）时会清空以防止出现过期缓存被查询使用的情况。当某个合约退订时，该合约的缓存 Tick 也会清空。
     */
    val lastTicks = mutableMapOf<String, DepthMarketData>()

    init {
        val cachePath = config.cachePath.ifBlank { "./data/ctp/" }
        val mdCachePath =
            "${if (cachePath.endsWith('/')) cachePath else "$cachePath/"}${config.investorId.ifBlank { "unknown" }}/md/"
        File(mdCachePath).mkdirs()
        mdApi = CThostFtdcMdApi.CreateFtdcMdApi(mdCachePath)
        mdSpi = CtpMdSpi()
        mdApi.RegisterSpi(mdSpi)
        config.mdFronts.forEach { mdFront ->
            mdApi.RegisterFront(mdFront)
        }
    }

    /**
     * 依据 [instrumentId] 获取完整的代码（ExchangeID.InstrumentID）
     */
    private fun getCode(instrumentId: String): String {
        return codeMap[instrumentId] ?: instrumentId
    }

    /**
     * 连接行情前置并自动完成登录。在无法连接至前置的情况下可能会长久阻塞。
     * 该操作不可加超时限制，因为可能在双休日等非交易时间段启动程序。
     */
    @JvmOverloads
    suspend fun connect(timeoutMills: Long? = null) {
        if (inited) return

        val tag = "connect"
        val block: (Continuation<Unit>) -> Unit = { continuation ->
            val requestId =
                Int.MIN_VALUE // 因为 OnFrontConnected 中 requestId 会重置为 0，为防止 requestId 重复，取整数最小值
            requestMap[requestId] = RequestContinuation(requestId, continuation, tag)
            postBrokerLogEvent(LogLevel.INFO, "【行情接口】$requestId 连接前置服务器...")
            mdApi.Init()
            inited = true
        }
        if (timeoutMills != null) {
            suspendCoroutineWithTimeout(tag, timeoutMills, block)
        } else {
            suspendCoroutine(block)
        }
    }

    /**
     * 关闭并释放资源，会发送一条 [BrokerEventType.CONNECTION] ([ConnectionEventType.MD_NET_DISCONNECTED]) 信息
     */
    fun close() {
        if (frontConnected) mdSpi.OnFrontDisconnected(0)
        subscriptions.clear()
        codeMap.clear()
        mdApi.Release()
        mdApi.delete()
    }

    /**
     * 获取当前交易日
     */
    fun tradingDay(): String {
        return if (connected) tradingDay else mdApi.GetTradingDay()
    }

    /**
     * 查询当前已订阅的合约。[useCache] 及 [extras] 参数暂时无用
     */
    fun querySubscriptions(): List<String> =
        subscriptions.toList()

    /**
     * 订阅行情。合约代码格式为 ExchangeID.InstrumentID。会自动检查合约订阅状态防止重复订阅。[extras.isForce: Boolean = false]【是否强制向交易所发送未更改的订阅请求（默认只发送未/已被订阅的标的的订阅请求）】
     */
    @JvmOverloads
    suspend fun subscribeMarketData(
        codes: Collection<String>,
        isForce: Boolean = false
    ) {
        if (codes.isEmpty()) return
        val filteredCodes = if (!isForce) codes.filter { it !in subscriptions } else codes
        if (filteredCodes.isEmpty()) return
        // CTP 行情订阅目前（2021.07）每34个订阅会丢失一个订阅（OnRspSubMarketData 中会每34个回调返回一个 bIsLast 为 true），所以需要分割
        if (filteredCodes.size >= 34) {
            val fullCodes = filteredCodes.toList()
            var startIndex = 0
            while (startIndex < filteredCodes.size) {
                subscribeMarketData(
                    fullCodes.subList(
                        startIndex,
                        min(startIndex + 33, filteredCodes.size)
                    )
                )
                startIndex += 33
            }
        } else { // codes 长度小于34，直接订阅
            val rawCodes = filteredCodes.map { code ->
                val instrumentId = parseCode(code).second
                if (codeMap[instrumentId] == null) codeMap[instrumentId] = code
                instrumentId
            }.toTypedArray()
            val requestId = nextRequestId()
            runWithResultCheck({
                postBrokerLogEvent(LogLevel.TRACE, "【行情接口】$requestId 订阅合约 $rawCodes...")
                mdApi.SubscribeMarketData(rawCodes)
            }, {
                val tag = "subscribeMarketData"
                suspendCoroutineWithTimeout<Unit>(
                    tag,
                    TIMEOUT_MILLS
                ) { continuation ->
                    // data 为订阅的 instrumentId 可变集合，在 CtpMdSpi.OnRspSubMarketData 中每收到一条合约订阅成功回报，就将该 instrumentId 从该可变集合中移除。当集合为空时，表明请求完成
                    requestMap[requestId] = RequestContinuation(
                        requestId,
                        continuation,
                        tag,
                        rawCodes.toMutableSet()
                    )
                }
            })
        }
    }

    /**
     * 退订行情。合约代码格式为 ExchangeID.InstrumentID。会自动检查合约订阅状态防止重复退订。[extras.isForce: Boolean = false]【是否强制向交易所发送未更改的订阅请求（默认只发送未/已被订阅的标的的订阅请求）】
     */
    @JvmOverloads
    suspend fun unsubscribeMarketData(
        codes: Collection<String>,
        isForce: Boolean = false
    ) {
        if (codes.isEmpty()) return
        val filteredCodes = if (!isForce) codes.filter { it in subscriptions } else codes
        if (filteredCodes.isEmpty()) return
        val rawCodes = filteredCodes.map { parseCode(it).second }.toTypedArray()
        val requestId = nextRequestId()
        runWithResultCheck({
            postBrokerLogEvent(LogLevel.TRACE, "【行情接口】$requestId 退订合约 $rawCodes...")
            mdApi.UnSubscribeMarketData(rawCodes)
        }, {
            val tag = "unsubscribeMarketData"
            suspendCoroutineWithTimeout<Unit>(
                tag,
                TIMEOUT_MILLS
            ) { continuation ->
                requestMap[requestId] = RequestContinuation(
                    requestId,
                    continuation,
                    tag,
                    rawCodes.toMutableSet()
                )
            }
        })
    }

    /**
     * 退订所有已订阅的合约行情。会自动检查合约订阅状态防止重复退订。[extras.isForce: Boolean = false]【是否强制向交易所发送未更改的订阅请求（默认只发送未/已被订阅的标的的订阅请求）】
     */
    suspend fun unsubscribeAllMarketData() {
        unsubscribeMarketData(subscriptions.toList())
    }

    /**
     * Ctp MdApi 的回调类
     */
    private inner class CtpMdSpi : CThostFtdcMdSpi() {

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
                    postBrokerLogEvent(LogLevel.ERROR, "【CtpMdSpi.OnRspError】$errorInfo")
                } else {
                    resumeRequestsWithException("connect", errorInfo)
                }
            } else {
                request.continuation.resumeWithException(Exception(pRspInfo.errorMsg))
                requestMap.remove(nRequestID)
            }
        }

        /**
         * 行情前置连接时回调。会将 [requestId] 置为 0；发送一条 [BrokerEventType.CONNECTION] 信息；自动请求用户登录 mdApi.ReqUserLogin（登录成功后 [connected] 才会置为 true），参见 [OnRspUserLogin]
         */
        override fun OnFrontConnected() {
            frontConnected = true
            requestId.set(0)
            postBrokerConnectionEvent(ConnectionEventType.MD_NET_CONNECTED)
            runBlocking {
                runWithResultCheck({
                    mdApi.ReqUserLogin(
                        CThostFtdcReqUserLoginField(),
                        nextRequestId()
                    )
                }, {}, { code, info ->
                    resumeRequestsWithException("connect", "请求用户登录失败：$info, $code")
                })
            }
        }

        /**
         * 行情前置断开连接时回调。会将 [connected] 置为 false；清空 [lastTicks]；发送一条 [BrokerEventType.CONNECTION] 信息；异常完成所有的协程请求
         */
        override fun OnFrontDisconnected(nReason: Int) {
            frontConnected = false
            connected = false
            lastTicks.clear()
            postBrokerConnectionEvent(
                ConnectionEventType.MD_NET_DISCONNECTED,
                "${getDisconnectReason(nReason)} ($nReason)"
            )
            val e = CtpException("网络连接断开：${getDisconnectReason(nReason)} ($nReason)")
            requestMap.values.forEach {
                it.continuation.resumeWithException(e)
            }
            requestMap.clear()
        }

        private val scope = CoroutineScope(Dispatchers.Default)

        /**
         * 用户登录结果回调。登录成功后 [connected] 会置为 true。如果判断是发生了日内断网重连，会自动重新订阅断连前的已订阅合约。如果交易日变更，已订阅列表会清空。
         */
        override fun OnRspUserLogin(
            pRspUserLogin: CThostFtdcRspUserLoginField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            checkRspInfo(pRspInfo, {
                if (pRspUserLogin == null) {
                    resumeRequestsWithException("connect", "请求用户登录失败：pRspUserLogin 为 null")
                    return
                }
                connected = true
                // 如果当日已订阅列表不为空，则说明发生了日内断网重连，自动重新订阅
                if (subscriptions.isNotEmpty() && tradingDay == pRspUserLogin.tradingDay) {
                    scope.launch {
                        runWithRetry({
                            subscribeMarketData(
                                codes = subscriptions.toList(),
                                isForce = true
                            )
                        }, { e ->
                            postBrokerLogEvent(
                                LogLevel.ERROR,
                                "【CtpMdSpi.OnRspUserLogin】重连后自动订阅行情失败：$e"
                            )
                        })
                    }
                }
                // 如果交易日变更，则清空当日已订阅列表
                if (tradingDay != pRspUserLogin.tradingDay) {
                    subscriptions.clear()
                    tradingDay = pRspUserLogin.tradingDay
                }
                postBrokerConnectionEvent(ConnectionEventType.MD_LOGGED_IN)
                resumeRequests("connect", Unit)
            }, { errorCode, errorMsg ->
                resumeRequestsWithException("connect", "请求用户登录失败：$errorMsg ($errorCode)")
            })
        }

        /**
         * 行情订阅结果回调。
         */
        override fun OnRspSubMarketData(
            pSpecificInstrument: CThostFtdcSpecificInstrumentField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            if (pSpecificInstrument == null) {
                resumeRequestsWithException(
                    "subscribeMarketData",
                    "请求订阅行情失败：pSpecificInstrument 为 null"
                )
                return
            }
            val instrumentId = pSpecificInstrument.instrumentID
            val code = getCode(instrumentId)
            checkRspInfo(pRspInfo, {
                postBrokerLogEvent(LogLevel.DEBUG, "【行情接口】$nRequestID 行情订阅成功：$code")
                subscriptions.add(code)
                resumeRequests("subscribeMarketData", Unit) { req ->
                    val subscribeSet = req.data as MutableSet<String>
                    subscribeSet.remove(instrumentId)
                    subscribeSet.isEmpty()
                }
            }, { errorCode, errorMsg ->
                resumeRequestsWithException(
                    "subscribeMarketData",
                    "请求订阅行情失败($code)：$errorMsg ($errorCode)"
                ) { req ->
                    (req.data as MutableSet<String>).contains(instrumentId)
                }
            })
        }

        /**
         * 行情退订结果回调。
         */
        override fun OnRspUnSubMarketData(
            pSpecificInstrument: CThostFtdcSpecificInstrumentField?,
            pRspInfo: CThostFtdcRspInfoField?,
            nRequestID: Int,
            bIsLast: Boolean
        ) {
            if (pSpecificInstrument == null) {
                resumeRequestsWithException(
                    "unsubscribeMarketData",
                    "请求退订行情失败：pSpecificInstrument 为 null"
                )
                return
            }
            val instrumentId = pSpecificInstrument.instrumentID
            val code = getCode(instrumentId)
            checkRspInfo(pRspInfo, {
                postBrokerLogEvent(LogLevel.DEBUG, "【行情接口】$nRequestID 行情退订成功：$code")
                subscriptions.remove(code)
                lastTicks.remove(code)
                resumeRequests("unsubscribeMarketData", Unit) { req ->
                    val subscribeSet = req.data as MutableSet<String>
                    subscribeSet.remove(instrumentId)
                    subscribeSet.isEmpty()
                }
            }, { errorCode, errorMsg ->
                resumeRequestsWithException(
                    "unsubscribeMarketData",
                    "请求退订行情失败($code)：$errorMsg ($errorCode)"
                ) { req ->
                    (req.data as MutableSet<String>).contains(instrumentId)
                }
            })
        }

        /**
         * 行情推送回调。行情会以 [BrokerEventType.TICK] 信息发送
         */
        override fun OnRtnDepthMarketData(data: CThostFtdcDepthMarketDataField) {
            val code = getCode(data.instrumentID)

            val depthMarketData = DepthMarketData.from(data)
            //返回的数据中没有 ExchangeID
            if (depthMarketData.exchangeID.isNullOrBlank()) {
                depthMarketData.exchangeID = parseCode(code).first
            }

            lastTicks[code] = depthMarketData
            postBrokerEvent(BrokerEventType.TICK, depthMarketData)
        }
    }
}