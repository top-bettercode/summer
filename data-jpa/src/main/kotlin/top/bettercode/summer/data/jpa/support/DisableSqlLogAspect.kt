package top.bettercode.summer.data.jpa.support

import ch.qos.logback.classic.Level
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.MDC_SQL_DISABLE_LOG
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.isShowSql
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.setSqlLevel


@Aspect
class DisableSqlLogAspect {

    private val logger: Logger = LoggerFactory.getLogger(DisableSqlLogAspect::class.java)


    @Around("@annotation(top.bettercode.summer.data.jpa.support.DisableSqlLog)")
    @Throws(Throwable::class)
    fun logProceed(joinPoint: ProceedingJoinPoint): Any? {
        return if (isShowSql()) {
            val sqlLevel = setSqlLevel(Level.INFO)
            try {
                MDC.put(MDC_SQL_DISABLE_LOG, "true")
                joinPoint.proceed()
            } finally {
                if (TransactionSynchronizationManager.isActualTransactionActive()) {
                    TransactionSynchronizationManager.registerSynchronization(object :
                        TransactionSynchronization {
                        override fun afterCommit() {
                            sqlLevel?.let { setSqlLevel(it) }
                            MDC.remove(MDC_SQL_DISABLE_LOG)
                        }
                    })
                } else {
                    sqlLevel?.let { setSqlLevel(it) }
                    MDC.remove(MDC_SQL_DISABLE_LOG)
                }
            }
        } else {
            joinPoint.proceed()
        }
    }
}
