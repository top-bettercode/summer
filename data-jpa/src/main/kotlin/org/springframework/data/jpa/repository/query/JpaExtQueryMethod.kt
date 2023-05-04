package org.springframework.data.jpa.repository.query

import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.session.Configuration
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.data.jpa.provider.QueryExtractor
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.util.Lazy
import top.bettercode.summer.data.jpa.support.QuerySize
import java.lang.reflect.Method

/**
 * @author Peter Wu
 */
class JpaExtQueryMethod(
        method: Method,
        metadata: RepositoryMetadata,
        factory: ProjectionFactory,
        extractor: QueryExtractor, configuration: Configuration?,
) : JpaQueryMethod(method, metadata, factory, extractor) {
    val statementId: String
    val mappedStatement: MappedStatement?
    private val querySize: Lazy<QuerySize>

    init {
        querySize = Lazy.of { AnnotatedElementUtils.findMergedAnnotation(method, QuerySize::class.java) }
        statementId = method.declaringClass.name + "." + method.name
        val mappedStatement: MappedStatement? = try {
            configuration!!.getMappedStatement(statementId)
        } catch (e: Exception) {
            null
        }
        this.mappedStatement = mappedStatement
    }

    fun getQuerySize(): QuerySize? {
        return querySize.nullable
    }
}
