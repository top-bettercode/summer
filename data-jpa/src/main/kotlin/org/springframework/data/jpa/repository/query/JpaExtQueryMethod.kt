package org.springframework.data.jpa.repository.query

import org.apache.ibatis.session.Configuration
import org.springframework.data.jpa.provider.QueryExtractor
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.RepositoryMetadata
import java.lang.reflect.Method

/**
 * @author Peter Wu
 */
class JpaExtQueryMethod(
        method: Method,
        metadata: RepositoryMetadata,
        factory: ProjectionFactory,
        extractor: QueryExtractor, configuration: Configuration
) : JpaQueryMethod(method, metadata, factory, extractor) {
    val statementId: String
    val mybatisQueryMethod: MybatisQueryMethod?

    init {
        statementId = method.declaringClass.name + "." + method.name
        val statement = try {
            configuration.getMappedStatement(statementId)
        } catch (e: Exception) {
            null
        }
        mybatisQueryMethod = if (statement != null) {
            MybatisQueryMethod(statement, isPageQuery, isSliceQuery, method)
        } else {
            null
        }
    }
}
