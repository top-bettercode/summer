package top.bettercode.summer.logging.async

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import top.bettercode.summer.tools.lang.operation.HttpOperation
import kotlin.random.Random

@Aspect
class MethodLoggingAspect {

    private val logger: Logger = LoggerFactory.getLogger(MethodLoggingAspect::class.java)

    @Around("@annotation(org.springframework.scheduling.annotation.Async) || @annotation(org.springframework.scheduling.annotation.Scheduled) || @annotation(top.bettercode.summer.logging.async.Loggable)")
    @Throws(Throwable::class)
    fun logProceed(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.toShortString()
        try {
            val traceid =
                MDC.get(HttpOperation.MDC_TRACEID) ?: Integer.toHexString(Random.nextInt())
            MDC.put(HttpOperation.MDC_TRACEID, traceid)
            logger.info("==={} started===", methodName)
            val result = joinPoint.proceed()
            logger.info("==={} finished===", methodName)
            return result
        } catch (e: Throwable) {
            logger.error("==={} error===", methodName, e)
            throw e
        } finally {
            MDC.remove(HttpOperation.MDC_TRACEID)
        }
    }
}
