package top.bettercode.summer.logging.async

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
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
        val signature = joinPoint.signature as MethodSignature
        val methodName = signature.toShortString()
        try {
            val method = signature.method
            val annotation = method.getAnnotation(Loggable::class.java)
            val level = annotation?.value ?: Level.INFO

            val startTime = System.currentTimeMillis()
            val traceid = HttpOperation.traceid()
            MDC.put(HttpOperation.MDC_TRACEID, traceid)
            log(level, "==={} started===", methodName)
            val result = joinPoint.proceed()
            val cost = System.currentTimeMillis() - startTime
            if (cost > QUERIES_SLOWER_THAN_MS) {
                logger.warn("==={} finished cost: {} ms===", methodName, cost)
            } else {
                log(level, "==={} finished cost: {} ms===", methodName, cost)
            }
            return result
        } catch (e: Throwable) {
            logger.error(AlarmMarker.noAlarmMarker, "===$methodName error===", e)
            throw e
        } finally {
            MDC.remove(HttpOperation.MDC_TRACEID)
        }
    }

    private fun log(level: Level, message: String, vararg arguments: Any) {
        when (level) {
            Level.TRACE -> logger.trace(message, *arguments)
            Level.DEBUG -> logger.debug(message, *arguments)
            Level.INFO -> logger.info(message, *arguments)
            Level.WARN -> logger.warn(message, *arguments)
            Level.ERROR -> logger.error(message, *arguments)
            else -> logger.info(message, *arguments)
        }
    }
}
