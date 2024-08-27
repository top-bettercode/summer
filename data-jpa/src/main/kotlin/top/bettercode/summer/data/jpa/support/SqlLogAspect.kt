package top.bettercode.summer.data.jpa.support

import ch.qos.logback.classic.Level
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.isShowSql
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.setSqlLevel


@Aspect
class SqlLogAspect {

    private val logger: Logger = LoggerFactory.getLogger(SqlLogAspect::class.java)


    @Around("@annotation(top.bettercode.summer.data.jpa.support.DisableSqlLog)")
    @Throws(Throwable::class)
    fun disableSqlLog(joinPoint: ProceedingJoinPoint): Any? {
        return if (isShowSql()) {
            val sqlLevel = setSqlLevel(Level.INFO)
            try {
                joinPoint.proceed()
            } finally {
                if (sqlLevel != null) {
                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(object :
                            TransactionSynchronization {
                            override fun afterCommit() {
                                setSqlLevel(sqlLevel)
                            }
                        })
                    } else {
                        setSqlLevel(sqlLevel)
                    }
                }
            }
        } else {
            joinPoint.proceed()
        }
    }

    @Around("@annotation(top.bettercode.summer.data.jpa.support.EnableSqlLog)")
    @Throws(Throwable::class)
    fun enableSqlLog(joinPoint: ProceedingJoinPoint): Any? {
        return if (isShowSql()) {
            joinPoint.proceed()
        } else {
            val sqlLevel = setSqlLevel(Level.DEBUG)
            try {
                joinPoint.proceed()
            } finally {
                if (sqlLevel != null) {
                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(object :
                            TransactionSynchronization {
                            override fun afterCommit() {
                                setSqlLevel(sqlLevel)
                            }
                        }
                        )
                    } else {
                        setSqlLevel(sqlLevel)
                    }
                }
            }
        }
    }
}
