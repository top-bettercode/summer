package top.bettercode.summer.ktrader.broker.ctp

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.rationalityfrontline.jctp.CThostFtdcMdApi
import org.rationalityfrontline.kevent.KEvent
import top.bettercode.summer.ktrader.broker.api.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 *
 * @author Peter Wu
 */
open class CtpApi(val config: CtpConfig, val kEvent: KEvent, val sourceId: String) {

    /**
     * 协程请求列表，每当网络断开（OnFrontDisconnected）时会清空（resumeWithException）
     */
    protected val requestMap: ConcurrentHashMap<Int, RequestContinuation> = ConcurrentHashMap()

    /**
     * 自增的请求 id，每当网络连接时（OnFrontConnected）重置为 0
     */
    protected val requestId: AtomicInteger = AtomicInteger(0)
    protected fun nextRequestId(): Int = requestId.incrementAndGet()

    /**
     * 上次更新的交易日。当 [connected] 处于 false 状态时可能因过期而失效
     */
    protected var tradingDay: String = ""

    /**
     * 是否已调用过 [CThostFtdcMdApi.Init]
     */
    protected var inited = false

    /**
     * 行情前置是否已连接
     */
    protected var frontConnected: Boolean = false

    /**
     * 是否已完成登录操作（即处于可用状态）
     */
    var connected: Boolean = false
        protected set

    /**
     * 将符合 [predicate] 条件的标签为 [tag] 的协程请求用 [result] 正常完成
     */
    protected fun <T> resumeRequests(
        tag: String,
        result: T,
        predicate: ((RequestContinuation) -> Boolean)? = null
    ) {
        requestMap.values.filter { it.tag == tag }.forEach { req ->
            if (predicate?.invoke(req) != false) {
                @Suppress("UNCHECKED_CAST")
                (req.continuation as Continuation<T>).resume(result)
                requestMap.remove(req.requestId)
            }
        }
    }


    /**
     * 将符合 [predicate] 条件的标签为 [tag] 的协程请求用 [errorInfo] 的报错信息异常完成
     */
    protected fun resumeRequestsWithException(
        tag: String,
        errorInfo: String,
        predicate: ((RequestContinuation) -> Boolean)? = null
    ) {
        requestMap.values.filter { it.tag == tag }.forEach { req ->
            if (predicate?.invoke(req) != false) {
                req.continuation.resumeWithException(Exception(errorInfo))
                requestMap.remove(req.requestId)
            }
        }
    }

    /**
     * [withTimeout] 与 [suspendCancellableCoroutine] 的结合简写
     */
    protected suspend inline fun <T> suspendCoroutineWithTimeout(
        tag: String,
        timeMills: Long,
        crossinline block: (CancellableContinuation<T>) -> Unit
    ): T? {
        try {
            return withTimeout(timeMills) {
                suspendCancellableCoroutine(block)
            }
        } catch (e: TimeoutCancellationException) {
            postBrokerLogEvent(LogLevel.WARNING, "$tag 超时:${e.message}")
            return null
        }
    }

    /**
     * 向 [kEvent] 发送一条 [BrokerEvent]
     */
    protected fun postBrokerEvent(type: BrokerEventType, data: Any) {
        kEvent.post(type, BrokerEvent(type, sourceId, data))
    }

    /**
     * 向 [kEvent] 发送一条 [BrokerEvent].[LogEvent]
     */
    protected fun postBrokerLogEvent(level: LogLevel, msg: String) {
        postBrokerEvent(BrokerEventType.LOG, LogEvent(level, msg))
    }

    /**
     * 向 [kEvent] 发送一条 [BrokerEvent].[ConnectionEvent]
     */
    protected fun postBrokerConnectionEvent(msgType: ConnectionEventType, msg: String = "") {
        postBrokerEvent(BrokerEventType.CONNECTION, ConnectionEvent(msgType, msg))
    }

}