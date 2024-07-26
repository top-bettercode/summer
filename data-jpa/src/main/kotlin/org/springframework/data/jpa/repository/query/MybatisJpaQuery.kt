package org.springframework.data.jpa.repository.query

import org.hibernate.query.NativeQuery
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.data.domain.SliceImpl
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.query.JpaQueryExecution.*
import org.springframework.data.jpa.repository.query.QueryParameterSetter.QueryMetadataCache
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.data.util.ParsingUtils
import org.springframework.util.Assert
import top.bettercode.summer.data.jpa.query.mybatis.CountSqlParser
import top.bettercode.summer.data.jpa.query.mybatis.MybatisQuery
import top.bettercode.summer.data.jpa.support.Size
import java.util.regex.Pattern
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.Query

class MybatisJpaQuery(method: JpaExtQueryMethod, em: EntityManager) : AbstractJpaQuery(method, em) {
    private val sqlLog = LoggerFactory.getLogger(MybatisJpaQuery::class.java)

    private val metadataCache = QueryMetadataCache()
    private val mybatisQueryMethod: MybatisQueryMethod = method.mybatisQueryMethod!!
    private val mybatisParameterBinder: MybatisParameterBinder by lazy {
        MybatisParameterBinder(
            queryMethod.parameters,
            mybatisQueryMethod.paramed,
            mybatisQueryMethod.mappedStatement
        )
    }
    private val queryExecution: JpaQueryExecution by lazy {
        val sqlLogId = mybatisQueryMethod.mappedStatement.id
        if (method.isPageQuery) {
            object : PagedExecution() {
                override fun doExecute(
                    repositoryQuery: AbstractJpaQuery,
                    accessor: JpaParametersParameterAccessor
                ): Any {
                    val mybatisQuery = repositoryQuery.createQuery(accessor) as MybatisQuery
                    val total: Long
                    val resultList: List<*>?
                    if (accessor.pageable.isPaged) {
                        val countQuery = mybatisQuery.countQuery!!
                        val totals = countQuery.resultList
                        total = if (totals.size == 1) CONVERSION_SERVICE.convert(
                            totals[0],
                            Long::class.java
                        ) ?: 0 else totals.size.toLong()
                        resultList = if (total > 0 && total > accessor.pageable.offset) {
                            mybatisQuery.resultList
                        } else {
                            emptyList<Any>()
                        }
                    } else {
                        resultList = mybatisQuery.resultList
                        total = resultList.size.toLong()
                    }
                    return PageableExecutionUtils.getPage(resultList, accessor.pageable) { total }
                }
            }
        } else if (method.isCollectionQuery) {
            CollectionExecution()
        } else if (mybatisQueryMethod.isModifyingQuery || method.isModifyingQuery) {
            ModifyingExecution(method, entityManager)
        } else if (method.isProcedureQuery) {
            ProcedureExecution()
        } else if (method.isStreamQuery) {
            StreamExecution()
        } else if (method.isSliceQuery) {
            object : SlicedExecution() {
                override fun doExecute(
                    query: AbstractJpaQuery,
                    accessor: JpaParametersParameterAccessor
                ): Any {
                    val pageable = accessor.pageable
                    val nestedResultMapType = mybatisQueryMethod.nestedResultMapType
                    if (pageable.isPaged && nestedResultMapType != null) {
                        if (nestedResultMapType.isCollection) {
                            throw UnsupportedOperationException(
                                nestedResultMapType.nestedResultMapId
                                        + " collection resultmap not support page query"
                            )
                        } else {
                            sqlLog.info(
                                "{} may return incorrect paginated data. Please check result maps definition {}.",
                                sqlLogId, nestedResultMapType.nestedResultMapId
                            )
                        }
                    }
                    return super.doExecute(query, accessor) as SliceImpl<*>
                }
            }
        } else {
            object : SingleEntityExecution() {
                @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
                override fun doExecute(
                    query: AbstractJpaQuery,
                    accessor: JpaParametersParameterAccessor
                ): Any? {
                    val result: Any? = try {
                        super.doExecute(query, accessor)
                    } catch (e: NoResultException) {
                        null
                    }
                    return result
                }
            }
        }
    }

    public override fun doCreateQuery(accessor: JpaParametersParameterAccessor): Query {
        val parameterBinder = parameterBinder.get() as MybatisParameterBinder
        val mybatisParam = parameterBinder.bindParameterObject(accessor)
        val boundSql = mybatisParam.boundSql
        val queryString = boundSql.sql

        val sort = accessor.sort
        val size = mybatisParam.size
        val sortedQueryString = applySorting(
            queryString,
            if (sort.isUnsorted && size != null) size.sort else sort
        )
        val query = entityManager.createNativeQuery(sortedQueryString)
        @Suppress("DEPRECATION")
        query.unwrap(NativeQuery::class.java)
            .setResultTransformer(mybatisQueryMethod.resultTransformer)
        val metadata = metadataCache.getMetadata(sortedQueryString, query)
        // it is ok to reuse the binding contained in the ParameterBinder although we create a new query String because the
        // parameters in the query do not change.
        if (mybatisQueryMethod.querySize != null && size == null) {
            mybatisParam.size = Size.of(mybatisQueryMethod.querySize)
        }
        val countQuery: Query? =
            if (accessor.pageable.isPaged) {
                val countMappedStatement = mybatisQueryMethod.countMappedStatement
                val countQueryString: String = if (countMappedStatement != null) {
                    val countBoundSql =
                        countMappedStatement.getBoundSql(mybatisParam.parameterObject)
                    countBoundSql.sql
                } else {
                    CountSqlParser.getSmartCountSql(queryString)
                }
                val countQuery = entityManager.createNativeQuery(countQueryString)
                val countMmetadata = metadataCache.getMetadata(countQueryString, countQuery)
                parameterBinder.bind(countMmetadata.withQuery(countQuery), mybatisParam)
                if (queryMethod.applyHintsToCountQuery()) applyHints(
                    countQuery,
                    queryMethod
                ) else countQuery
            } else null

        return parameterBinder.bindAndPrepare(
            MybatisQuery(query, countQuery),
            metadata, accessor, mybatisParam
        )
    }

    override fun doCreateCountQuery(accessor: JpaParametersParameterAccessor): Query {
        throw UnsupportedOperationException()
    }

    override fun createBinder(): ParameterBinder {
        return mybatisParameterBinder
    }

    override fun getExecution(): JpaQueryExecution {
        return queryExecution
    }

    companion object {

        private val CONVERSION_SERVICE: ConversionService

        init {
            val conversionService = DefaultConversionService()

            conversionService.addConverter(JpaResultConverters.BlobToByteArrayConverter.INSTANCE)
            conversionService.removeConvertible(Collection::class.java, Object::class.java)
            potentiallyRemoveOptionalConverter(conversionService)

            CONVERSION_SERVICE = conversionService
        }

        fun convertOrderBy(sort: Sort?): String? {
            return if (sort == null || !sort.isSorted) {
                null
            } else sort.map { o: Sort.Order ->
                ParsingUtils.reconcatenateCamelCase(
                    o.property,
                    "_"
                ) + " " + o.direction
            }.joinToString(",")
        }

        private val ORDER_BY = Pattern.compile(
            "[\\s\\S]*order\\s+by\\s+[\\s\\S]*",
            Pattern.CASE_INSENSITIVE
        )

        fun applySorting(query: String, sort: Sort?): String {
            Assert.hasText(query, "Query must not be null or empty!")
            if (sort!!.isUnsorted) {
                return query
            }
            val builder = StringBuilder(query)
            if (!ORDER_BY.matcher(query).matches()) {
                builder.append(" order by ")
            } else {
                builder.append(", ")
            }
            builder.append(convertOrderBy(sort))
            return builder.toString()
        }
    }
}
