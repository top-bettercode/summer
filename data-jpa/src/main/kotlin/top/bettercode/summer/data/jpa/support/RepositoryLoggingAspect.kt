package top.bettercode.summer.data.jpa.support

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.aop.framework.Advised
import org.springframework.data.domain.Page
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.affected
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.cost
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.limit
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.offset
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.retrieved
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.total
import java.lang.reflect.Method
import javax.persistence.EntityManager


@Aspect
class RepositoryLoggingAspect {

    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")
    private val logger: Logger = LoggerFactory.getLogger(RepositoryLoggingAspect::class.java)

    @Around("this(top.bettercode.summer.data.jpa.BaseRepository)")
    @Throws(Throwable::class)
    fun logProceed(joinPoint: ProceedingJoinPoint): Any? {
        if (sqlLog.isDebugEnabled) {
            val repository = joinPoint.target

            repository as Advised
            val target = repository.targetSource.target
            val targetClass = target.javaClass
            val getAdices = targetClass.getMethod("getAdvices")
            val advices = getAdices.invoke(target) as Map<*, *>

            val methodSignature: MethodSignature = joinPoint.signature as MethodSignature
            val method: Method = methodSignature.method
            val logAdice = advices[method] as LogAdvice
            val sqlId = logAdice.sqlId
            MDC.put(SqlAppender.MDC_SQL_ID, sqlId)
            val pageable = logAdice.pageable(joinPoint.args)
            val modify = logAdice.isModify
            try {
                val startMillis = System.currentTimeMillis()
                try {
                    if (pageable != null && pageable.isPaged) {
                        val pageSize = pageable.pageSize
                        val offset = pageable.offset
                        sqlLog.offset(offset)
                        sqlLog.limit(pageSize)
                    }
                    val result = joinPoint.proceed()
                    when (result) {
                        is Number -> {
                            if (modify) {
                                sqlLog.affected(result)
                            } else {
                                sqlLog.total(result)
                            }
                        }

                        is Page<*> -> {
                            sqlLog.total(result.totalElements)
                            sqlLog.retrieved(result.size)
                        }

                        is Collection<*> -> {
                            sqlLog.retrieved(result.size)
                        }

                        else -> {
                        }
                    }
                    return result
                } catch (e: Exception) {
                    MDC.put(SqlAppender.MDC_SQL_ERROR, e.stackTraceToString())
                    throw e
                } finally {
                    //? 自动提交，对性能的影响
                    if (modify) {
                        val entityManager = targetClass.getMethod("getEntityManager")
                            .invoke(target) as EntityManager
                        if (entityManager.isJoinedToTransaction) {
                            val flushMethod = targetClass.getMethod("flush")
                            flushMethod.invoke(target)
                        }
                    }
                    val duration = System.currentTimeMillis() - startMillis
                    sqlLog.cost(duration)
                }
            } finally {
                MDC.remove(SqlAppender.MDC_SQL_ERROR)
                MDC.remove(SqlAppender.MDC_SQL_ID)
            }
        } else {
            return joinPoint.proceed()
        }
    }
}
