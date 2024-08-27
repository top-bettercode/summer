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
import org.springframework.util.ClassUtils
import top.bettercode.summer.data.jpa.support.LoggerInfo
import top.bettercode.summer.data.jpa.support.PageInfo
import top.bettercode.summer.data.jpa.support.QuerySize
import top.bettercode.summer.data.jpa.support.Size
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.affected
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.cost
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.limit
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.offset
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.result
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.retrieved
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.total
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
            val sqlId =
                "$className.${method.name}(${
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
            val annoPageInfo: PageInfo?
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
            val isModify: Boolean
            val repositoryQuery = queries[method] as RepositoryQuery?
            if (repositoryQuery != null) {
                val queryMethod = repositoryQuery.queryMethod as JpaExtQueryMethod
                isModify =
                    queryMethod.isModifyingQuery || queryMethod.mybatisQueryMethod?.isModifyingQuery == true
            } else {
                //save update delete
                isModify = method.name.startsWith("save")
                        || method.name.startsWith("update")
                        || method.name.startsWith("delete")
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
                val startMillis = System.currentTimeMillis()
                try {
                    MDC.put(SqlAppender.MDC_SQL_ID, logAdice.sqlId)
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
                    val duration = System.currentTimeMillis() - startMillis
                    sqlLog.cost(duration)
                    MDC.remove(SqlAppender.MDC_SQL_ERROR)
                    MDC.remove(SqlAppender.MDC_SQL_ID)
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