package org.springframework.data.repository.core.support

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.aop.framework.Advised
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.query.JpaExtQueryMethod
import org.springframework.data.repository.query.RepositoryQuery
import top.bettercode.summer.data.jpa.support.LoggerInfo
import top.bettercode.summer.data.jpa.support.PageInfo
import top.bettercode.summer.data.jpa.support.QuerySize
import top.bettercode.summer.data.jpa.support.Size
import top.bettercode.summer.tools.lang.log.SqlAppender
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.affected
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.cost
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.limit
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.offset
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.retrieved
import top.bettercode.summer.tools.lang.log.SqlAppender.Companion.total
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
        val methods = repositoryInterface.methods
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
            var offsetIndex = -1
            if (annoPageInfo != null) {
                pageableIndex = -1
            } else {
                pageableIndex = parameters.indexOfFirst { it.type == Pageable::class.java }
                if (pageableIndex < 0)
                    pageableIndex = parameters.indexOfFirst { it.type == Size::class.java }
                if (pageableIndex < 0 && !currentClass) {
                    pageableIndex =
                        parameters.indexOfFirst { it.name == "size" && (it.type == Int::class.java || it.type == Int::class.javaObjectType) }
                    offsetIndex =
                        parameters.indexOfFirst { it.name == "offset" && (it.type == Long::class.java || it.type == Long::class.javaObjectType) }
                }
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
                offsetIndex = offsetIndex,
                isModify = isModify
            )
        }

    }

    override fun invoke(invocation: MethodInvocation): Any? {
        if (sqlLog.isDebugEnabled) {
            val method: Method = invocation.method
            val logAdice = loggerInfos[method] as LoggerInfo
            val sqlId = logAdice.sqlId
            MDC.put(SqlAppender.MDC_SQL_ID, sqlId)
            val pageInfo = logAdice.pageable(invocation.arguments)
            val modify = logAdice.isModify
            try {
                val startMillis = System.currentTimeMillis()
                try {
                    if (pageInfo != null) {
                        sqlLog.offset(pageInfo.offset)
                        sqlLog.limit(pageInfo.size)
                    }
                    val result = invocation.proceed()
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
                    if (modify && SqlAppender.isAutoFlush()) {
                        if (entityManager.isJoinedToTransaction) {
                            val flushMethod = repositoryClass.getMethod("flush")
                            flushMethod.invoke(repository)
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
            return invocation.proceed()
        }
    }
}