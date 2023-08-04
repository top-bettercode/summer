package org.springframework.data.jpa.repository.query

import org.slf4j.MDC
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.query.JpaParameters.JpaParameter
import org.springframework.data.jpa.repository.query.JpaQueryExecution.DeleteExecution
import org.springframework.data.jpa.repository.query.JpaQueryExecution.ExistsExecution
import org.springframework.data.jpa.repository.query.QueryParameterSetter.QueryMetadataCache
import org.springframework.data.repository.query.ReturnedType
import org.springframework.data.repository.query.parser.Part
import org.springframework.data.repository.query.parser.PartTree
import org.springframework.data.repository.query.parser.PartTree.OrPart
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import top.bettercode.summer.data.jpa.query.JpaQueryLogExecution
import top.bettercode.summer.data.jpa.support.DefaultExtJpaSupport
import top.bettercode.summer.data.jpa.support.ExtJpaSupport
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery

/**
 * @author Peter Wu
 */
internal class PartTreeJpaExtQuery internal constructor(
        method: JpaExtQueryMethod, private val em: EntityManager, private val escape: EscapeCharacter,
        jpaExtProperties: JpaExtProperties
) : AbstractJpaQuery(method, em) {
    private var tree: PartTree
    private val parameters: JpaParameters
    private var query: QueryPreparer
    private var countQuery: QueryPreparer
    private val softDeleteSupport: ExtJpaSupport
    private val statementId: String?

    init {
        statementId = method.statementId
        val domainClass = method.entityInformation.javaType
        softDeleteSupport = DefaultExtJpaSupport(jpaExtProperties, domainClass)
        parameters = method.parameters
        val recreationRequired = parameters.hasDynamicProjection() || parameters.potentiallySortsDynamically()
        try {
            tree = PartTree(method.name, domainClass)
            validate(tree, parameters, method.toString())
            countQuery = CountQueryPreparer(recreationRequired)
            query = if (tree.isCountProjection) countQuery else QueryPreparer(recreationRequired)
        } catch (e: Exception) {
            throw IllegalArgumentException(String.format("Failed to create query for method %s! %s", method, e.message), e)
        }
    }

    /*
   * (non-Javadoc)
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateQuery(JpaParametersParameterAccessor)
   */
    public override fun doCreateQuery(accessor: JpaParametersParameterAccessor): Query {
        return query.createQuery(accessor)
    }

    /*
   * (non-Javadoc)
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateCountQuery(JpaParametersParameterAccessor)
   */
    public override fun doCreateCountQuery(accessor: JpaParametersParameterAccessor): TypedQuery<Long> {
        @Suppress("UNCHECKED_CAST")
        return countQuery.createQuery(accessor) as TypedQuery<Long>
    }

    /*
   * (non-Javadoc)
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#getExecution()
   */
    override fun getExecution(): JpaQueryExecution {
        if (tree.isDelete) {
            return object : DeleteExecution(em) {
                override fun doExecute(
                        jpaQuery: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return try {
                        MDC.put("id", statementId)
                        val query = jpaQuery.createQuery(accessor)
                        val resultList = query.resultList
                        if (softDeleteSupport.supportSoftDeleted()) {
                            for (o in resultList) {
                                softDeleteSupport.setSoftDeleted(o!!)
                                em.merge(o)
                            }
                        } else {
                            for (o in resultList) {
                                em.remove(o)
                            }
                        }
                        if (jpaQuery.queryMethod.isCollectionQuery) resultList else resultList.size
                    } finally {
                        MDC.remove("id")
                    }
                }
            }
        } else if (tree.isExistsProjection) {
            return object : ExistsExecution() {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return try {
                        MDC.put("id", statementId)
                        super.doExecute(query, accessor)
                    } finally {
                        MDC.remove("id")
                    }
                }
            }
        }
        return JpaQueryLogExecution(super.getExecution(), statementId)
    }

    /**
     * Query preparer to create [CriteriaQuery] instances and potentially cache them.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private open inner class QueryPreparer(recreateQueries: Boolean) {
            private var cachedCriteriaQuery: CriteriaQuery<*>? = null

            private var cachedParameterBinder: ParameterBinder? = null
        private val metadataCache = QueryMetadataCache()

        init {
            val creator = this.createCreator(null)
            if (recreateQueries) {
                cachedCriteriaQuery = null
                cachedParameterBinder = null
            } else {
                cachedCriteriaQuery = creator.createQuery()
                cachedParameterBinder = getBinder(creator.parameterExpressions)
            }
        }

        /**
         * Creates a new [Query] for the given parameter values.
         */
        fun createQuery(accessor: JpaParametersParameterAccessor): Query {
            var criteriaQuery = cachedCriteriaQuery
            var parameterBinder = cachedParameterBinder
            if (cachedCriteriaQuery == null || accessor.hasBindableNullValue()) {
                val creator = createCreator(accessor)
                criteriaQuery = creator.createQuery(getDynamicSort(accessor))
                val expressions = creator.parameterExpressions
                parameterBinder = getBinder(expressions)
            }
            checkNotNull(parameterBinder) { "ParameterBinder is null!" }
            val query = createQuery(criteriaQuery)
            return restrictMaxResultsIfNecessary(
                    invokeBinding(parameterBinder, query, accessor, metadataCache))
        }

        /**
         * Restricts the max results of the given [Query] if the current `tree` marks this
         * `query` as limited.
         */
        private fun restrictMaxResultsIfNecessary(query: Query): Query {
            if (tree.isLimiting) {
                val maxResults = tree.maxResults!!
                if (query.maxResults != Int.MAX_VALUE) {
                    /*
           * In order to return the correct results, we have to adjust the first result offset to be returned if:
           * - a Pageable parameter is present
           * - AND the requested page number > 0
           * - AND the requested page size was bigger than the derived result limitation via the First/Top keyword.
           */
                    if (query.maxResults > maxResults && query.firstResult > 0) {
                        query.setFirstResult(
                                query.firstResult - (query.maxResults - maxResults))
                    }
                }
                query.setMaxResults(maxResults)
            }
            if (tree.isExistsProjection) {
                query.setMaxResults(1)
            }
            return query
        }

        /**
         * Checks whether we are working with a cached [CriteriaQuery] and synchronizes the
         * creation of a [TypedQuery] instance from it. This is due to non-thread-safety in the
         * [CriteriaQuery] implementation of some persistence providers (i.e. Hibernate in this
         * case), see DATAJPA-396.
         *
         * @param criteriaQuery must not be null.
         */
        private fun createQuery(criteriaQuery: CriteriaQuery<*>?): TypedQuery<*> {
            if (cachedCriteriaQuery != null) {
                synchronized(cachedCriteriaQuery!!) { return entityManager.createQuery(criteriaQuery) }
            }
            return entityManager.createQuery(criteriaQuery)
        }

        protected open fun createCreator(accessor: JpaParametersParameterAccessor?): JpaQueryCreator {
            val entityManager = entityManager
            val builder = entityManager.criteriaBuilder
            val processor = queryMethod.resultProcessor
            val provider: ParameterMetadataProvider
            val returnedType: ReturnedType
            if (accessor != null) {
                provider = ParameterMetadataProvider(builder, accessor, escape)
                returnedType = processor.withDynamicProjection(accessor).returnedType
            } else {
                provider = ParameterMetadataProvider(builder, parameters, escape)
                returnedType = processor.returnedType
            }
            return JpaExtQueryCreator(tree, returnedType, builder, provider, softDeleteSupport)
        }

        /**
         * Invokes parameter binding on the given [TypedQuery].
         */
        protected open fun invokeBinding(
                binder: ParameterBinder, query: TypedQuery<*>,
                accessor: JpaParametersParameterAccessor,
                metadataCache: QueryMetadataCache
        ): Query {
            val metadata = metadataCache.getMetadata("query", query)
            return binder.bindAndPrepare(query, metadata, accessor)
        }

        private fun getBinder(expressions: List<ParameterMetadataProvider.ParameterMetadata<*>>): ParameterBinder {
            return ParameterBinderFactory.createCriteriaBinder(parameters, expressions)
        }

        private fun getDynamicSort(accessor: JpaParametersParameterAccessor): Sort {
            return if (parameters.potentiallySortsDynamically() //
            ) accessor.sort //
            else Sort.unsorted()
        }
    }

    /**
     * Special [QueryPreparer] to create count queries.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private inner class CountQueryPreparer(recreateQueries: Boolean) : QueryPreparer(recreateQueries) {
        override fun createCreator(accessor: JpaParametersParameterAccessor?): JpaQueryCreator {
            val entityManager = entityManager
            val builder = entityManager.criteriaBuilder
            val provider: ParameterMetadataProvider = accessor?.let { ParameterMetadataProvider(builder, it, escape) }
                    ?: ParameterMetadataProvider(builder, parameters, escape)
            return JpaExtCountQueryCreator(tree,
                    queryMethod.resultProcessor.returnedType, builder, provider,
                    softDeleteSupport)
        }

        /**
         * Customizes binding by skipping the pagination.
         */
        override fun invokeBinding(
                binder: ParameterBinder, query: TypedQuery<*>,
                accessor: JpaParametersParameterAccessor,
                metadataCache: QueryMetadataCache
        ): Query {
            val metadata = metadataCache.getMetadata("countquery", query)
            return binder.bind(query, metadata, accessor)
        }
    }

    companion object {
        private fun validate(tree: PartTree, parameters: JpaParameters, methodName: String) {
            var argCount = 0
            val parts = Iterable { tree.stream().flatMap { obj: OrPart -> obj.stream() }.iterator() }
            for (part in parts) {
                val numberOfArguments = part.numberOfArguments
                for (i in 0 until numberOfArguments) {
                    throwExceptionOnArgumentMismatch(methodName, part, parameters, argCount)
                    argCount++
                }
            }
        }

        private fun throwExceptionOnArgumentMismatch(
                methodName: String, part: Part,
                parameters: JpaParameters,
                index: Int
        ) {
            val type = part.type
            val property = part.property.toDotPath()
            check(parameters.bindableParameters.hasParameterAt(index)) {
                String.format(
                        "Method %s expects at least %d arguments but only found %d. This leaves an operator of type %s for property %s unbound.",
                        methodName, index + 1, index, type.name, property)
            }
            val parameter = parameters.getBindableParameter(index)
            check(!(expectsCollection(type) && !parameterIsCollectionLike(parameter))) { wrongParameterTypeMessage(methodName, property, type, "Collection", parameter) }
            check(!(!expectsCollection(type) && !parameterIsScalarLike(parameter))) { wrongParameterTypeMessage(methodName, property, type, "scalar", parameter) }
        }

        private fun wrongParameterTypeMessage(
                methodName: String, property: String,
                operatorType: Part.Type,
                expectedArgumentType: String, parameter: JpaParameter
        ): String {
            return String.format("Operator %s on %s requires a %s argument, found %s in method %s.",
                    operatorType.name,
                    property, expectedArgumentType, parameter.type, methodName)
        }

        private fun parameterIsCollectionLike(parameter: JpaParameter): Boolean {
            return Iterable::class.java.isAssignableFrom(parameter.type) || parameter.type.isArray
        }

        /**
         * Arrays are may be treated as collection like or in the case of binary data as scalar
         */
        private fun parameterIsScalarLike(parameter: JpaParameter): Boolean {
            return !Iterable::class.java.isAssignableFrom(parameter.type)
        }

        private fun expectsCollection(type: Part.Type): Boolean {
            return type == Part.Type.IN || type == Part.Type.NOT_IN
        }
    }
}