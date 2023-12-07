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
import top.bettercode.summer.data.jpa.support.JpaUtil
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.persistence.EntityManager
import javax.persistence.Query

class MybatisJpaQuery(method: JpaExtQueryMethod, em: EntityManager) : AbstractJpaQuery(method, em) {
    private val sqlLog = LoggerFactory.getLogger("org.hibernate.SQL")

    private val metadataCache = QueryMetadataCache()
    private val mybatisQueryMethod: MybatisQueryMethod = method.mybatisQueryMethod!!

    public override fun doCreateQuery(accessor: JpaParametersParameterAccessor): Query {
        val parameterBinder = parameterBinder.get() as MybatisParameterBinder
        val mybatisParam = parameterBinder.bindParameterObject(accessor)
        val boundSql = mybatisParam.boundSql
        val queryString = boundSql.sql

        val sort = accessor.sort
        val size = mybatisParam.size
        val sortedQueryString = applySorting(queryString,
                if (sort.isUnsorted && size != null) size.sort else sort)
        val query = entityManager.createNativeQuery(sortedQueryString)
        @Suppress("DEPRECATION")
        query.unwrap(NativeQuery::class.java).setResultTransformer(mybatisQueryMethod.resultTransformer)
        val metadata = metadataCache.getMetadata(sortedQueryString, query)
        // it is ok to reuse the binding contained in the ParameterBinder although we create a new query String because the
        // parameters in the query do not change.
        if (mybatisQueryMethod.querySize != null) {
            query.setFirstResult(0)
            query.setMaxResults(mybatisQueryMethod.querySize)
        }
        val countQuery: Query? =
                if (accessor.pageable.isPaged) {
                    val countMappedStatement = mybatisQueryMethod.countMappedStatement
                    val countQueryString: String = if (countMappedStatement != null) {
                        val countBoundSql = countMappedStatement.getBoundSql(mybatisParam.parameterObject)
                        countBoundSql.sql
                    } else {
                        CountSqlParser.getSmartCountSql(queryString)
                    }
                    val countQuery = entityManager.createNativeQuery(countQueryString)
                    val countMmetadata = metadataCache.getMetadata(countQueryString, countQuery)
                    parameterBinder.bind(countMmetadata.withQuery(countQuery), mybatisParam)
                    if (queryMethod.applyHintsToCountQuery()) applyHints(countQuery, queryMethod) else countQuery
                } else null

        return parameterBinder.bindAndPrepare(MybatisQuery(query, countQuery),
                metadata, accessor, mybatisParam)
    }

    override fun doCreateCountQuery(accessor: JpaParametersParameterAccessor): Query {
        throw UnsupportedOperationException()
    }

    override fun createBinder(): ParameterBinder {
        return MybatisParameterBinder(queryMethod.parameters, mybatisQueryMethod.paramed, mybatisQueryMethod.mappedStatement)
    }

    override fun getExecution(): JpaQueryExecution {
        val method = queryMethod
        val sqlLogId = mybatisQueryMethod.mappedStatement.id
        return if (method.isPageQuery) {
            object : PagedExecution() {
                private val CONVERSION_SERVICE: ConversionService

                init {
                    val conversionService = DefaultConversionService()

                    conversionService.addConverter(JpaResultConverters.BlobToByteArrayConverter.INSTANCE)
                    conversionService.removeConvertible(Collection::class.java, Object::class.java)
                    potentiallyRemoveOptionalConverter(conversionService)

                    CONVERSION_SERVICE = conversionService
                }

                override fun doExecute(
                        repositoryQuery: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return JpaUtil.mdcId(sqlLogId) {
                        val mybatisQuery = repositoryQuery.createQuery(accessor) as MybatisQuery
                        val total: Long
                        val resultList: List<*>?
                        if (accessor.pageable.isPaged) {
                            val countQuery = mybatisQuery.countQuery!!
                            val totals = countQuery.resultList
                            total = if (totals.size == 1) CONVERSION_SERVICE.convert(totals[0], Long::class.java)
                                    ?: 0 else totals.size.toLong()
                            if (sqlLog.isDebugEnabled) {
                                sqlLog.debug("total: {} rows", total)
                            }
                            if (total > 0 && total > accessor.pageable.offset) {
                                resultList = mybatisQuery.resultList
                                if (sqlLog.isDebugEnabled) {
                                    sqlLog.debug("{} rows retrieved", resultList.size)
                                }
                            } else {
                                resultList = emptyList<Any>()
                            }
                        } else {
                            resultList = mybatisQuery.resultList
                            if (sqlLog.isDebugEnabled) {
                                sqlLog.debug("{} rows retrieved", resultList.size)
                            }
                            total = resultList.size.toLong()
                        }
                        PageableExecutionUtils.getPage(resultList, accessor.pageable) { total }
                    }
                }
            }
        } else if (method.isCollectionQuery) {
            object : CollectionExecution() {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return JpaUtil.mdcId(sqlLogId) {
                        val result = super.doExecute(query, accessor) as List<*>
                        if (sqlLog.isDebugEnabled) {
                            sqlLog.debug("{} rows retrieved", result.size)
                        }
                        result
                    }
                }
            }
        } else if (mybatisQueryMethod.isModifyingQuery || method.isModifyingQuery) {
            object : ModifyingExecution(method, entityManager) {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return JpaUtil.mdcId(sqlLogId) {
                        val result = super.doExecute(query, accessor)
                        if (sqlLog.isDebugEnabled) {
                            sqlLog.debug("{} row affected", result)
                        }
                        result
                    }
                }
            }
        } else if (method.isProcedureQuery) {
            object : ProcedureExecution() {
                override fun doExecute(
                        jpaQuery: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return JpaUtil.mdcId(sqlLogId) {
                        super.doExecute(jpaQuery, accessor)
                    }
                }
            }
        } else if (method.isStreamQuery) {
            object : StreamExecution() {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return JpaUtil.mdcId(sqlLogId) {
                        super.doExecute(query, accessor)
                    }
                }
            }
        } else if (method.isSliceQuery) {
            object : SlicedExecution() {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return JpaUtil.mdcId(sqlLogId) {
                        val pageable = accessor.pageable
                        val nestedResultMapType = mybatisQueryMethod.nestedResultMapType
                        if (pageable.isPaged && nestedResultMapType != null) {
                            if (nestedResultMapType.isCollection) {
                                throw UnsupportedOperationException(nestedResultMapType.nestedResultMapId
                                        + " collection resultmap not support page query")
                            } else {
                                sqlLog.info(
                                        "{} may return incorrect paginated data. Please check result maps definition {}.",
                                        sqlLogId, nestedResultMapType.nestedResultMapId)
                            }
                        }
                        val result = super.doExecute(query, accessor) as SliceImpl<*>
                        if (sqlLog.isDebugEnabled) {
                            sqlLog.debug("total: {} rows", result.numberOfElements)
                            sqlLog.debug("{} rows retrieved", result.size)
                        }
                        result
                    }
                }
            }
        } else {
            object : SingleEntityExecution() {
                @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
                override fun doExecute(query: AbstractJpaQuery, accessor: JpaParametersParameterAccessor): Any? {
                    return JpaUtil.mdcId(sqlLogId) {
                        val result: Any? = super.doExecute(query, accessor)
                        if (sqlLog.isDebugEnabled) {
                            sqlLog.debug("{} rows retrieved", if (result == null) 0 else 1)
                        }
                        result
                    }
                }
            }
        }
    }

    companion object {

        fun convertOrderBy(sort: Sort?): String? {
            return if (sort == null || !sort.isSorted) {
                null
            } else sort.stream().map { o: Sort.Order -> ParsingUtils.reconcatenateCamelCase(o.property, "_") + " " + o.direction }
                    .collect(
                            Collectors.joining(","))
        }

        private val ORDER_BY = Pattern.compile("[\\s\\S]*order\\s+by\\s+[\\s\\S]*",
                Pattern.CASE_INSENSITIVE)

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
