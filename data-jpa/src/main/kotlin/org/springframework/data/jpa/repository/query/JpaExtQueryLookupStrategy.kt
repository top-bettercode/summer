package org.springframework.data.jpa.repository.query

import org.apache.ibatis.session.Configuration
import org.springframework.data.jpa.provider.QueryExtractor
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.NamedQueries
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.util.Assert
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import java.lang.reflect.Method
import javax.persistence.EntityManager

/**
 * Query lookup strategy to execute finders.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
object JpaExtQueryLookupStrategy {
    /**
     * Creates a [QueryLookupStrategy] for the given [EntityManager] and [Key].
     *
     * @param em                        must not be null.
     * @param key                       may be null.
     * @param extractor                 must not be null.
     * @param evaluationContextProvider must not be null.
     * @param escape                    escape
     * @param jpaExtProperties          jpaExtProperties
     * @param configuration             configuration
     * @return QueryLookupStrategy
     */
    fun create(
            em: EntityManager, configuration: Configuration,
            key: QueryLookupStrategy.Key?,
            extractor: QueryExtractor,
            evaluationContextProvider: QueryMethodEvaluationContextProvider,
            escape: EscapeCharacter,
            jpaExtProperties: JpaExtProperties
    ): QueryLookupStrategy {
        Assert.notNull(em, "EntityManager must not be null!")
        Assert.notNull(extractor, "QueryExtractor must not be null!")
        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!")
        return when (key ?: QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND) {
            QueryLookupStrategy.Key.CREATE -> CreateQueryLookupStrategy(em, extractor, escape, jpaExtProperties,
                    configuration)

            QueryLookupStrategy.Key.USE_DECLARED_QUERY -> DeclaredQueryLookupStrategy(em, extractor, evaluationContextProvider,
                    configuration)

            QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND -> CreateIfNotFoundQueryLookupStrategy(em, extractor,
                    CreateQueryLookupStrategy(em, extractor, escape, jpaExtProperties, configuration),
                    DeclaredQueryLookupStrategy(em, extractor, evaluationContextProvider,
                            configuration))

            else -> throw IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key))
        }
    }

    /**
     * Base class for [QueryLookupStrategy] implementations that need access to an [ ].
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private abstract class AbstractQueryLookupStrategy(
            private val em: EntityManager, private val provider: QueryExtractor,
            val configuration: Configuration
    ) : QueryLookupStrategy {

        /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
     */
        override fun resolveQuery(
                method: Method, metadata: RepositoryMetadata,
                factory: ProjectionFactory,
                namedQueries: NamedQueries
        ): RepositoryQuery {
            return resolveQuery(
                    JpaExtQueryMethod(method, metadata, factory, provider, configuration), em,
                    namedQueries)
        }

        protected abstract fun resolveQuery(
                method: JpaExtQueryMethod, em: EntityManager,
                namedQueries: NamedQueries
        ): RepositoryQuery
    }

    /**
     * [QueryLookupStrategy] to create a query from the method name.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class CreateQueryLookupStrategy(
            em: EntityManager, extractor: QueryExtractor,
            private val escape: EscapeCharacter,
            private val jpaExtProperties: JpaExtProperties, configuration: Configuration
    ) : AbstractQueryLookupStrategy(em, extractor, configuration) {
        public override fun resolveQuery(
                method: JpaExtQueryMethod, em: EntityManager,
                namedQueries: NamedQueries
        ): RepositoryQuery {
            return PartTreeJpaExtQuery(method, em, escape, jpaExtProperties)
        }
    }

    /**
     * [QueryLookupStrategy] that tries to detect a declared query declared via [Query]
     * annotation followed by a JPA named query lookup.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class DeclaredQueryLookupStrategy(
            em: EntityManager,
            extractor: QueryExtractor,
            private val evaluationContextProvider: QueryMethodEvaluationContextProvider,
            configuration: Configuration
    ) : AbstractQueryLookupStrategy(em, extractor, configuration) {
        public override fun resolveQuery(
                method: JpaExtQueryMethod, em: EntityManager,
                namedQueries: NamedQueries
        ): RepositoryQuery {
            var query: RepositoryQuery? = JpaQueryFactory.INSTANCE
                    .fromQueryAnnotation(method, em, evaluationContextProvider)
            if (null != query) {
                return query
            }
            query = JpaQueryFactory.INSTANCE.fromProcedureAnnotation(method, em)
            if (null != query) {
                return query
            }
            val name: String = method.namedQueryName
            if (namedQueries.hasQuery(name)) {
                query = JpaQueryFactory.INSTANCE
                        .fromMethodWithQueryString(method, em, namedQueries.getQuery(name),
                                evaluationContextProvider)
                if (null != query) {
                    return query
                }
            }
            query = NamedQuery.lookupFrom(method, em)
            if (null != query) {
                return query
            }
            throw IllegalStateException(
                    java.lang.String.format("Did neither find a NamedQuery nor an annotated query for method %s!",
                            method))
        }
    }

    /**
     * [QueryLookupStrategy] to try to detect a declared query first ( [ ], JPA named query). In case none is found we fall
     * back on query creation.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class CreateIfNotFoundQueryLookupStrategy(
            em: EntityManager, extractor: QueryExtractor,
            private val createStrategy: CreateQueryLookupStrategy, private val lookupStrategy: DeclaredQueryLookupStrategy
    ) : AbstractQueryLookupStrategy(em, extractor, lookupStrategy.configuration) {
        /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy.AbstractQueryLookupStrategy#resolveQuery(org.springframework.data.jpa.repository.query.JpaQueryMethod, javax.persistence.EntityManager, org.springframework.data.repository.core.NamedQueries)
     */
        override fun resolveQuery(
                method: JpaExtQueryMethod, em: EntityManager,
                namedQueries: NamedQueries
        ): RepositoryQuery {
            return try {
                lookupStrategy.resolveQuery(method, em, namedQueries)
            } catch (e: IllegalStateException) {
                if (method.mappedStatement != null) {
                    MybatisJpaQuery(method, em)
                } else {
                    createStrategy.resolveQuery(method, em, namedQueries)
                }
            }
        }
    }
}