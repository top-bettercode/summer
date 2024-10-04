package top.bettercode.summer.logging.async

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import top.bettercode.summer.tools.lang.log.AlarmMarker
import top.bettercode.summer.tools.lang.operation.HttpOperation

@Aspect
class MethodLoggingAspect {

    private val logger: Logger = LoggerFactory.getLogger("METHOD_PROCEED")

    companion object {
        var QUERIES_SLOWER_THAN_MS = 2000
    }

    @Around("@annotation(org.springframework.scheduling.annotation.Async) || @annotation(org.springframework.scheduling.annotation.Scheduled) || @annotation(top.bettercode.summer.logging.async.Loggable)")
    @Throws(Throwable::class)
    fun logProceed(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.toShortString()
        try {
            val startTime = System.currentTimeMillis()
            val traceid = HttpOperation.traceid()
            MDC.put(HttpOperation.MDC_TRACEID, traceid)
            logger.info("==={} started===", methodName)
            val result = joinPoint.proceed()
            val cost = System.currentTimeMillis() - startTime
            if (cost > QUERIES_SLOWER_THAN_MS) {
                logger.warn("==={} finished cost: {} ms===", methodName, cost)
            } else {
                logger.info("==={} finished cost: {} ms===", methodName, cost)
            }
            return result
        } catch (e: Throwable) {
            logger.error(AlarmMarker.noAlarmMarker, "===$methodName error===", e)
            throw e
        } finally {
            MDC.remove(HttpOperation.MDC_TRACEID)
        }
    }
}
