@file:Suppress("JoinDeclarationAndAssignment")

package top.bettercode.summer.ktrader.broker.ctp

import kotlinx.coroutines.TimeoutCancellationException
import org.rationalityfrontline.jctp.CThostFtdcTraderApi
import org.rationalityfrontline.kevent.KEvent
import top.bettercode.summer.ktrader.broker.api.BrokerEvent
import top.bettercode.summer.ktrader.broker.api.BrokerEventType
import top.bettercode.summer.ktrader.broker.api.LogEvent
import top.bettercode.summer.ktrader.broker.api.LogLevel
import top.bettercode.summer.ktrader.datatype.*

/**
 * [BrokerApi] 的 CTP 实现
 */
class CtpBrokerApi(val config: CtpConfig, val kEvent: KEvent) {

    val mdApi: CtpMdApi
    val tdApi: CtpTdApi

    val name: String = config.name
    val version: String = CThostFtdcTraderApi.GetApiVersion()
    val account: String = this.config.investorId
    val mdConnected: Boolean get() = mdApi.connected
    val tdConnected: Boolean get() = tdApi.connected

    /**
     * 是否行情及交易接口均已连接
     */
    val connected: Boolean get() = mdConnected && tdConnected

    /**
     * 唯一标识该 [BrokerApi] 实例的字段，默认实现为 "${name}_${account}_${hashCode()}"
     */
    val sourceId: String get() = "${name}_${account}_${hashCode()}"

    init {
        mdApi = CtpMdApi(this.config, kEvent, sourceId)
        tdApi = CtpTdApi(this.config, kEvent, sourceId)
        mdApi.tdApi = tdApi
        tdApi.mdApi = mdApi
    }

    /**
     * 向 [kEvent] 发送一条 [BrokerEvent]
     */
    private fun postBrokerEvent(type: BrokerEventType, data: Any) {
        kEvent.post(type, BrokerEvent(type, sourceId, data))
    }

    /**
     * 向 [kEvent] 发送一条 [BrokerEvent].[LogEvent]
     */
    private fun postBrokerLogEvent(level: LogLevel, msg: String) {
        postBrokerEvent(BrokerEventType.LOG, LogEvent(level, msg))
    }

    suspend fun connect(timeoutMills: Long? = null) {
        if (connected) {
            return
        }
        postBrokerLogEvent(LogLevel.INFO, "【CtpBrokerApi.connect】开始连接")
        try {
            if (!mdConnected) mdApi.connect(timeoutMills)
        } catch (e: TimeoutCancellationException) {
            postBrokerLogEvent(LogLevel.WARNING, "【CtpMdApi.connect】连接超时失败")
        }
        if (!tdConnected) tdApi.connect(timeoutMills)
        postBrokerLogEvent(LogLevel.INFO, "【CtpBrokerApi.connect】连接成功")
    }

    fun close() {
        postBrokerLogEvent(LogLevel.INFO, "【CtpBrokerApi.close】开始关闭")
        tdApi.close()
        mdApi.close()
        postBrokerLogEvent(LogLevel.INFO, "【CtpBrokerApi.close】关闭成功")
    }

    fun getTradingDay(): String {
        val tradingDay = when {
            mdConnected -> mdApi.tradingDay()
            tdConnected -> tdApi.tradingDay()
            else -> null
        }
        return tradingDay ?: throw CtpException("行情前置与交易前置均不可用，无法获取当前交易日")
    }

    suspend fun subscribeTicks(codes: Collection<String>) {
        mdApi.subscribeMarketData(codes)
    }

    suspend fun unsubscribeTicks(codes: Collection<String>) {
        mdApi.unsubscribeMarketData(codes)
    }

    suspend fun unsubscribeAllTicks() {
        mdApi.unsubscribeAllMarketData()
    }

    suspend fun subscribeTick(code: String) {
        subscribeTicks(listOf(code))
    }

    suspend fun unsubscribeTick(code: String) {
        unsubscribeTicks(listOf(code))
    }

    /**
     * [useCache] 无效，总是查询本地维护的数据，CTP 无此查询接口
     */
    fun queryTickSubscriptions(): List<String> {
        return mdApi.querySubscriptions()
    }

    @JvmOverloads
    suspend fun queryLastTick(
        code: String,
        useCache: Boolean = true
    ): DepthMarketData? {
        return runWithRetry({ tdApi.queryLastTick(code, useCache) })
    }

    suspend fun queryProduct(
        productId: String
    ): Product? {
        return runWithRetry({ tdApi.queryProduct(productId) })
    }

    suspend fun querySecurity(
        code: String,
    ): Instrument? {
        return runWithRetry({ tdApi.queryInstrument(code) })
    }

    suspend fun queryAllProducts(): List<Product>? {
        return runWithRetry({ tdApi.queryAllProducts() })
    }

    suspend fun queryAllSecurities(): List<Instrument>? {
        return runWithRetry({ tdApi.queryAllInstruments() })
    }

    @JvmOverloads
    suspend fun queryTrades(
        code: String? = null,
    ): List<Trade>? {
        return runWithRetry({ tdApi.queryTrades(code) })
    }

    suspend fun queryAssets(): TradingAccount? {
        return runWithRetry({ tdApi.queryAssets() })
    }

    @JvmOverloads
    suspend fun queryPositions(
        code: String? = null,
        direction: Direction? = null,
    ): List<InvestorPosition>? {
        return runWithRetry({ tdApi.queryPositions(code, direction) })
    }

    @JvmOverloads
    suspend fun queryPositionDetails(
        code: String? = null,
        direction: Direction? = null,
    ): List<InvestorPositionDetail>? {
        return runWithRetry({ tdApi.queryPositionDetails(code, direction) })
    }

    @JvmOverloads
    suspend fun queryPositionCombineDetails(
        code: String? = null,
    ): List<InvestorPositionCombineDetail>? {
        return runWithRetry({ tdApi.queryPositionCombineDetails(code) })
    }

    @JvmOverloads
    suspend fun queryFuturesCommissionRate(code: String? = null): List<InstrumentCommissionRate>? {
        return runWithRetry({ tdApi.queryFuturesCommissionRate(code) })
    }
}