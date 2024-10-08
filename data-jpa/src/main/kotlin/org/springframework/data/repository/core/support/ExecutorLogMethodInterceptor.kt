package org.springframework.data.repository.core.support

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.aop.framework.Advised
import org.springframework.core.NestedExceptionUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.query.JpaExtQueryMethod
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.util.ClassUtils
import top.bettercode.summer.data.jpa.support.LoggerInfo
import top.bettercode.summer.data.jpa.support.PageInfo
import top.bettercode.summer.data.jpa.support.QuerySize
import top.bettercode.summer.data.jpa.support.Size
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.MDC_SQL_LIMIT
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.MDC_SQL_OFFSET
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.affected
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.end
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.limit
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.offset
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.result
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.retrieved
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.start
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.total
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.operation.HttpOperation.MDC_TRACEID
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.persistence.EntityManager
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/**
 *
 * @author Peter Wu
 */
class ExecutorLogMethodInterceptor(
    repositoryInterface: Class<*>,
    repository: Any,
    private val entityManager: EntityManager
) :
    MethodInterceptor {

    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")

    private val repositoryClass: Class<*>
    private val repository: Any
    private val loggerInfos: Map<Method, LoggerInfo>

    init {
        repository as Advised
        val target = repository.targetSource.target!!
        this.repository = target
        this.repositoryClass = target.javaClass

        val queryExecutorMethodInterceptor =
            repository.advisors.find { it.advice is QueryExecutorMethodInterceptor }!!.advice

        val queriesProperty =
            QueryExecutorMethodInterceptor::class.declaredMemberProperties.find { it.name == "queries" }!!
        queriesProperty.isAccessible = true
        val queries = queriesProperty.getter.call(queryExecutorMethodInterceptor) as Map<*, *>

        val className = repositoryInterface.simpleName
        val methods = repositoryInterface.methods.filter {
            !arrayOf(
                "clear",
                "detach",
                "flush",
                "getEntityManager"
            ).contains(it.name)
        }
        loggerInfos = methods.associateWith { method ->
            val parameters = method.parameters
            val methodName = method.name
            val sqlId =
                "$className.$methodName(${
                    parameters.joinToString(", ") { param ->
                        "${param.type.simpleName}${
                            if (param.type.typeParameters.isNotEmpty()) "<${
                                param.type.typeParameters.joinToString(
                                    ", "
                                ) { it.typeName }
                            }>" else ""
                        }${if (param.type.isArray) "[]" else ""}"
                    }
                })"
            var annoPageInfo: PageInfo?
            val declaringClass = method.declaringClass
            val currentClass = declaringClass == repositoryInterface
            if (currentClass) {
                val querySize = AnnotationUtils.getAnnotation(method, QuerySize::class.java)
                annoPageInfo = if (querySize != null) PageInfo(size = querySize.value) else null
            } else {
                annoPageInfo = null
            }
            var pageableIndex: Int
            if (annoPageInfo != null) {
                pageableIndex = -1
            } else {
                pageableIndex = parameters.indexOfFirst { it.type == Pageable::class.java }
                if (pageableIndex < 0)
                    pageableIndex = parameters.indexOfFirst { it.type == Size::class.java }
            }
            if (pageableIndex < 0 && annoPageInfo == null && methodName.startsWith("findFirst")) {
                annoPageInfo = PageInfo(size = 1)
            }

            val isModify: Boolean
            val repositoryQuery = queries[method] as RepositoryQuery?
            if (repositoryQuery != null) {
                val queryMethod = repositoryQuery.queryMethod as JpaExtQueryMethod
                isModify =
                    queryMethod.isModifyingQuery || queryMethod.mybatisQueryMethod?.isModifyingQuery == true
            } else {
                //save update delete
                isModify = methodName.startsWith("save")
                        || methodName.startsWith("update")
                        || methodName.startsWith("delete")
                        || methodName.startsWith("persist")
            }
            LoggerInfo(
                sqlId = sqlId,
                annoPageInfo = annoPageInfo,
                pageableIndex = pageableIndex,
                isModify = isModify
            )
        }

    }

    override fun invoke(invocation: MethodInvocation): Any? {
        if (sqlLog.isDebugEnabled) {
            val logAdice = loggerInfos[invocation.method]
            if (logAdice == null) {
                return invocation.proceed()
            } else {
                val traceId = MDC.get(MDC_TRACEID)
                try {
                    MDC.put(MDC_TRACEID, HttpOperation.appendTraceid())
                    MDC.put(SqlAppender.MDC_SQL_ID, logAdice.sqlId)
                    sqlLog.start()
                    val modify = logAdice.isModify
                    val pageInfo = logAdice.pageable(invocation.arguments)
                    if (pageInfo != null) {
                        sqlLog.offset(pageInfo.offset)
                        sqlLog.limit(pageInfo.size)
                    }
                    val result = invocation.proceed()
                    when {

                        result is Page<*> -> {
                            sqlLog.total(result.totalElements)
                            sqlLog.retrieved(result.content.size)
                        }

                        result is Collection<*> -> {
                            sqlLog.retrieved(result.size)
                        }

                        result is Number -> {
                            if (modify) {
                                sqlLog.affected(result.toString())
                            }
                        }

                        (result == null && !isVoidType(invocation.method.returnType))
                                || result != null && ClassUtils.isPrimitiveOrWrapper(result::class.java) -> {
                            sqlLog.result(result.toString())
                        }

                        else -> {
                        }
                    }
                    if (modify && entityManager.isJoinedToTransaction) {
                        val flushMethod = repositoryClass.getMethod("flush")
                        flushMethod.invoke(repository)
                    }
                    return result
                } catch (e: Exception) {
                    MDC.put(
                        SqlAppender.MDC_SQL_ERROR,
                        (NestedExceptionUtils.getRootCause(e) ?: e).message ?: e.message
                    )
                    if (e is InvocationTargetException) {
                        throw e.targetException ?: e
                    } else {
                        throw e
                    }
                } finally {
                    sqlLog.end()
                    MDC.remove(SqlAppender.MDC_SQL_ID)
                    MDC.remove(MDC_SQL_OFFSET)
                    MDC.remove(MDC_SQL_LIMIT)
                    MDC.remove(SqlAppender.MDC_SQL_ERROR)

                    if (TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(object :
                            TransactionSynchronization {
                            override fun afterCommit() {
                                MDC.put(MDC_TRACEID, traceId)
                            }
                        }
                        )
                    }
                }
            }
        } else {
            return invocation.proceed()
        }
    }

    private fun isVoidType(clazz: Class<*>): Boolean {
        return clazz == Void.TYPE || clazz == Void::class.java
    }

}