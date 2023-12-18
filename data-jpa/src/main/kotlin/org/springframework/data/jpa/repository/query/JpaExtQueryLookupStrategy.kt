package org.springframework.data.jpa.repository.query

import jakarta.persistence.EntityManager
import org.apache.ibatis.session.Configuration
import org.slf4j.LoggerFactory
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.provider.QueryExtractor
import org.springframework.data.jpa.repository.QueryRewriter
import org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy.NoQuery
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.NamedQueries
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.lang.Nullable
import org.springframework.util.Assert
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import java.lang.reflect.Method

/**
 * Query lookup strategy to execute finders.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
object JpaExtQueryLookupStrategy {
    private val LOG = LoggerFactory.getLogger(JpaQueryLookupStrategy::class.java)
    private val NO_QUERY: RepositoryQuery = NoQuery()
    fun create(
        em: EntityManager,
        @Suppress("UNUSED_PARAMETER") queryMethodFactory: JpaQueryMethodFactory,
        @Nullable key: QueryLookupStrategy.Key?,
        evaluationContextProvider: QueryMethodEvaluationContextProvider,
        queryRewriterProvider: QueryRewriterProvider, escape: EscapeCharacter,
        configuration: Configuration,
        extractor: QueryExtractor,
        jpaExtProperties: JpaExtProperties,
        auditorAware: AuditorAware<*>
    ): QueryLookupStrategy {
        Assert.notNull(em, "EntityManager must not be null")
        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null")
        Assert.notNull(extractor, "QueryExtractor must not be null!")
        return when (key ?: QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND) {
            QueryLookupStrategy.Key.CREATE -> CreateQueryLookupStrategy(
                em,
                queryRewriterProvider,
                extractor,
                configuration,
                escape,
                jpaExtProperties,
                auditorAware
            )

            QueryLookupStrategy.Key.USE_DECLARED_QUERY -> DeclaredQueryLookupStrategy(
                em, queryRewriterProvider, extractor, configuration,
                evaluationContextProvider
            )

            QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND -> CreateIfNotFoundQueryLookupStrategy(
                em, queryRewriterProvider, extractor,
                configuration,
                CreateQueryLookupStrategy(
                    em, queryRewriterProvider, extractor, configuration,
                    escape, jpaExtProperties, auditorAware
                ),
                DeclaredQueryLookupStrategy(
                    em, queryRewriterProvider, extractor, configuration,
                    evaluationContextProvider
                )
            )

            else -> throw IllegalArgumentException(
                String.format(
                    "Unsupported query lookup strategy %s",
                    key
                )
            )
        }
    }

    private abstract class AbstractQueryLookupStrategy(
        em: EntityManager,
        queryRewriterProvider: QueryRewriterProvider, provider: QueryExtractor,
        configuration: Configuration
    ) : QueryLookupStrategy {
        private val em: EntityManager
        private val queryRewriterProvider: QueryRewriterProvider
        private val provider: QueryExtractor
        val configuration: Configuration

        init {
            Assert.notNull(em, "EntityManager must not be null")
            this.em = em
            this.queryRewriterProvider = queryRewriterProvider
            this.provider = provider
            this.configuration = configuration
        }

        override fun resolveQuery(
            method: Method, metadata: RepositoryMetadata,
            factory: ProjectionFactory,
            namedQueries: NamedQueries
        ): RepositoryQuery {
            val queryMethod = JpaExtQueryMethod(
                method, metadata, factory, provider,
                configuration
            )
            return resolveQuery(
                queryMethod, queryRewriterProvider.getQueryRewriter(queryMethod), em,
                namedQueries
            )
        }

        abstract fun resolveQuery(
            method: JpaExtQueryMethod,
            queryRewriter: QueryRewriter,
            em: EntityManager, namedQueries: NamedQueries
        ): RepositoryQuery
    }

    private class CreateQueryLookupStrategy(
        em: EntityManager,
        queryRewriterProvider: QueryRewriterProvider,
        provider: QueryExtractor,
        configuration: Configuration,
        private val escape: EscapeCharacter,
        private val jpaExtProperties: JpaExtProperties,
        private val auditorAware: AuditorAware<*>
    ) : AbstractQueryLookupStrategy(em, queryRewriterProvider, provider, configuration) {
        override fun resolveQuery(
            method: JpaExtQueryMethod,
            queryRewriter: QueryRewriter,
            em: EntityManager, namedQueries: NamedQueries
        ): RepositoryQuery {
            return PartTreeJpaExtQuery(method, em, escape, jpaExtProperties, auditorAware)
        }
    }

    /**
     * [QueryLookupStrategy] that tries to detect a declared query declared via [RepositoryQuery]
     * annotation followed by a JPA named query lookup.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class DeclaredQueryLookupStrategy(
        em: EntityManager,
        queryRewriterProvider: QueryRewriterProvider,
        provider: QueryExtractor,
        configuration: Configuration,
        private val evaluationContextProvider: QueryMethodEvaluationContextProvider
    ) : AbstractQueryLookupStrategy(em, queryRewriterProvider, provider, configuration) {
        override fun resolveQuery(
            method: JpaExtQueryMethod, queryRewriter: QueryRewriter,
            em: EntityManager,
            namedQueries: NamedQueries
        ): RepositoryQuery {
            if (method.isProcedureQuery) {
                return JpaQueryFactory.INSTANCE.fromProcedureAnnotation(method, em)
            }
            if (!method.annotatedQuery.isNullOrBlank()) {
                if (method.hasAnnotatedQueryName()) {
                    LOG.warn(
                        String.format(
                            "Query method %s is annotated with both, a query and a query name. Using the declared query.",
                            method
                        )
                    )
                }
                return JpaQueryFactory.INSTANCE.fromMethodWithQueryString(
                    method,
                    em,
                    method.requiredAnnotatedQuery,
                    getCountQuery(method, namedQueries, em),
                    queryRewriter,
                    evaluationContextProvider
                )
            }
            val name = method.namedQueryName
            if (namedQueries.hasQuery(name)) {
                return JpaQueryFactory.INSTANCE.fromMethodWithQueryString(
                    method,
                    em,
                    namedQueries.getQuery(name),
                    getCountQuery(method, namedQueries, em),
                    queryRewriter,
                    evaluationContextProvider
                )
            }
            val query = NamedQuery.lookupFrom(method, em)
            return query ?: NO_QUERY
        }

        @Nullable
        private fun getCountQuery(
            method: JpaQueryMethod, namedQueries: NamedQueries,
            em: EntityManager
        ): String? {
            if (!method.countQuery.isNullOrBlank()) {
                return method.countQuery
            }
            val queryName = method.namedCountQueryName
            if (queryName.isNullOrBlank()) {
                return method.countQuery
            }
            if (namedQueries.hasQuery(queryName)) {
                return namedQueries.getQuery(queryName)
            }
            val namedQuery = NamedQuery.hasNamedQuery(em, queryName)
            return if (namedQuery) {
                method.queryExtractor.extractQueryString(em.createNamedQuery(queryName))
            } else null
        }
    }

    /**
     * [QueryLookupStrategy] to try to detect a declared query first (
     * [org.springframework.data.jpa.repository.Query], JPA named query). In case none is found
     * we fall back on query creation.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class CreateIfNotFoundQueryLookupStrategy(
        em: EntityManager,
        queryRewriterProvider: QueryRewriterProvider,
        provider: QueryExtractor,
        configuration: Configuration,
        private val createStrategy: CreateQueryLookupStrategy,
        private val lookupStrategy: DeclaredQueryLookupStrategy
    ) : AbstractQueryLookupStrategy(em, queryRewriterProvider, provider, configuration) {
        override fun resolveQuery(
            method: JpaExtQueryMethod, queryRewriter: QueryRewriter,
            em: EntityManager,
            namedQueries: NamedQueries
        ): RepositoryQuery {
            val lookupQuery = lookupStrategy.resolveQuery(
                method, queryRewriter, em,
                namedQueries
            )
            if (lookupQuery !== NO_QUERY) {
                return lookupQuery
            }
            return if (method.mybatisQueryMethod != null) {
                MybatisJpaQuery(method, em)
            } else {
                createStrategy.resolveQuery(method, queryRewriter, em, namedQueries)
            }
        }
    }
}
