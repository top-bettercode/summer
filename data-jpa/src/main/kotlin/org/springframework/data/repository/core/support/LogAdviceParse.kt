package org.springframework.data.repository.core.support

import org.springframework.aop.framework.Advised
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.query.JpaExtQueryMethod
import org.springframework.data.repository.query.RepositoryQuery
import top.bettercode.summer.data.jpa.support.LogAdvice
import top.bettercode.summer.data.jpa.support.QuerySize
import top.bettercode.summer.data.jpa.support.Size
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/**
 *
 * @author Peter Wu
 */
object LogAdviceParse {


    fun <T : Any> parse(repositoryInterface: Class<T>, repository: T) {
        val className = repositoryInterface.simpleName
        repository as Advised
        val queryExecutorMethodInterceptor =
            repository.advisors.find { it.advice is QueryExecutorMethodInterceptor }!!.advice
        val queriesProperty =
            QueryExecutorMethodInterceptor::class.declaredMemberProperties.find { it.name == "queries" }!!
        queriesProperty.isAccessible = true
        val queries = queriesProperty.getter.call(queryExecutorMethodInterceptor) as Map<*, *>

        val methods = repositoryInterface.methods
        val advices = methods.associateWith { method ->
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
            val annoPageable: Pageable?
            val declaringClass = method.declaringClass
            val currentClass = declaringClass == repositoryInterface
            if (currentClass) {
                val querySize = AnnotationUtils.getAnnotation(method, QuerySize::class.java)
                annoPageable = if (querySize != null) Pageable.ofSize(querySize.value) else null
            } else {
                annoPageable = null
            }
            var pageableIndex: Int
            if (annoPageable != null) {
                pageableIndex = -1
            } else {
                pageableIndex = parameters.indexOfFirst { it.type == Pageable::class.java }
                if (pageableIndex < 0)
                    pageableIndex = parameters.indexOfFirst { it.type == Size::class.java }
                if (pageableIndex < 0 && !currentClass)
                    pageableIndex =
                        parameters.indexOfFirst { it.name == "size" && (it.type == Int::class.java || it.type == Int::class.javaObjectType) }
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
            LogAdvice(
                sqlId = sqlId,
                annoPageable = annoPageable,
                pageableIndex = pageableIndex,
                isModify = isModify
            )
        }

        val target = repository.targetSource.target
        val targetClass = target.javaClass
        val setAdvices = targetClass.getMethod("setAdvices", Map::class.java)
        setAdvices.invoke(target, advices)
    }
}