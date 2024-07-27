package top.bettercode.summer.data.jpa.support

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.log.SqlAppender

@Aspect
class DisableSqlLogAspect {

    private val logger: Logger = LoggerFactory.getLogger(DisableSqlLogAspect::class.java)


    @Around("@annotation(top.bettercode.summer.data.jpa.support.DisableSqlLog)")
    @Throws(Throwable::class)
    fun logProceed(joinPoint: ProceedingJoinPoint): Any? {
        return SqlAppender.disableLog {
            joinPoint.proceed()
        }
    }
}
