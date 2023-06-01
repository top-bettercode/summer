package org.springframework.data.jpa.repository.query

import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.hibernate.query.NativeQuery
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.data.domain.SliceImpl
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.query.JpaQueryExecution.*
import org.springframework.data.jpa.repository.query.QueryParameterSetter.QueryMetadataCache
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.data.util.ParsingUtils
import org.springframework.util.Assert
import top.bettercode.summer.data.jpa.query.mybatis.*
import top.bettercode.summer.data.jpa.support.JpaUtil
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.persistence.*

class MybatisJpaQuery(method: JpaExtQueryMethod, em: EntityManager) : AbstractJpaQuery(method, em) {
    private val sqlLog = LoggerFactory.getLogger("org.hibernate.SQL")
    private val metadataCache = QueryMetadataCache()
    private val mappedStatement: MappedStatement
    private val countMappedStatement: MappedStatement?
    private val countSqlParser = CountSqlParser()
    private var nestedResultMapType: NestedResultMapType? = null
    private val resultTransformer: MybatisResultTransformer
    private val isModifyingQuery: Boolean
    private val querySize: Int?

    init {
        mappedStatement = method.mappedStatement!!
        val sqlCommandType = mappedStatement.sqlCommandType
        isModifyingQuery = SqlCommandType.UPDATE == sqlCommandType || SqlCommandType.DELETE == sqlCommandType || SqlCommandType.INSERT == sqlCommandType
        MybatisResultSetHandler.Companion.validateResultMaps(mappedStatement)
        resultTransformer = MybatisResultTransformer(mappedStatement)
        val pageQuery = method.isPageQuery
        if (pageQuery) {
            val nestedResultMapId: String? = MybatisResultSetHandler.Companion.findNestedResultMap(mappedStatement)
            if (nestedResultMapId != null) {
                sqlLog.info(
                        "{} may return incorrect paginated data. Please check result maps definition {}.",
                        mappedStatement.id, nestedResultMapId)
            }
        }
        nestedResultMapType = if (method.isSliceQuery) {
            MybatisResultSetHandler.Companion.findNestedResultMapType(mappedStatement)
        } else {
            null
        }
        val countMappedStatement: MappedStatement?
        if (pageQuery) {
            countMappedStatement = try {
                mappedStatement.configuration
                        .getMappedStatement(mappedStatement.id + "_COUNT")
            } catch (ignored: Exception) {
                null
            }
            this.countMappedStatement = countMappedStatement
        } else {
            this.countMappedStatement = null
        }
        val querySize = method.getQuerySize()
        if (querySize != null) {
            val value = querySize.value
            Assert.isTrue(value > 0, "size 必须大于0")
            this.querySize = value
        } else {
            this.querySize = null
        }
    }

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
        query.unwrap(NativeQuery::class.java).setResultTransformer(resultTransformer)
        val metadata = metadataCache.getMetadata(sortedQueryString, query)
        // it is ok to reuse the binding contained in the ParameterBinder although we create a new query String because the
        // parameters in the query do not change.
        if (querySize != null) {
            query.setFirstResult(0)
            query.setMaxResults(querySize)
        }
        return parameterBinder.bindAndPrepare(MybatisQuery(queryString, query, mybatisParam),
                metadata, accessor, mybatisParam)
    }

    override fun doCreateCountQuery(accessor: JpaParametersParameterAccessor): Query {
        throw UnsupportedOperationException()
    }

    override fun createBinder(): ParameterBinder {
        return MybatisParameterBinder(queryMethod.parameters, mappedStatement)
    }

    override fun getExecution(): JpaQueryExecution {
        val method = queryMethod
        val sqlLogId = mappedStatement.id
        return if (method.isPageQuery) {
            object : PagedExecution() {
                override fun doExecute(
                        repositoryQuery: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return try {
                        MDC.put("id", sqlLogId)
                        val mybatisQuery = repositoryQuery.createQuery(accessor) as MybatisQuery
                        val total: Long
                        val resultList: List<*>?
                        if (accessor.pageable.isPaged) {
                            val queryMethod = queryMethod
                            var countQueryString: String? = null
                            val mybatisParam = mybatisQuery.mybatisParam
                            if (countMappedStatement != null) {
                                val boundSql = countMappedStatement.getBoundSql(
                                        mybatisParam.parameterObject)
                                countQueryString = boundSql.sql
                            }
                            val queryString = countQueryString ?: countSqlParser.getSmartCountSql(
                                    mybatisQuery.queryString)
                            val em = entityManager
                            var countQuery = em.createNativeQuery(queryString)
                            val metadata = metadataCache.getMetadata(queryString,
                                    countQuery)
                            (parameterBinder.get() as MybatisParameterBinder).bind(metadata.withQuery(countQuery),
                                    mybatisParam)
                            countQuery = if (queryMethod.applyHintsToCountQuery()) applyHints(countQuery, queryMethod) else countQuery
                            val totals = countQuery.resultList
                            total = if (totals.size == 1) JpaUtil.convert(totals[0], Long::class.java)!! else totals.size.toLong()
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
                    } finally {
                        MDC.remove("id")
                    }
                }
            }
        } else if (method.isCollectionQuery) {
            object : CollectionExecution() {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return try {
                        MDC.put("id", sqlLogId)
                        val result = super.doExecute(query, accessor) as List<*>
                        if (sqlLog.isDebugEnabled) {
                            sqlLog.debug("{} rows retrieved", result.size)
                        }
                        result
                    } finally {
                        MDC.remove("id")
                    }
                }
            }
        } else if (isModifyingQuery || method.isModifyingQuery) {
            object : ModifyingExecution(method, entityManager) {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return try {
                        MDC.put("id", sqlLogId)
                        val result = super.doExecute(query, accessor)
                        if (sqlLog.isDebugEnabled) {
                            sqlLog.debug("{} row affected", result)
                        }
                        result
                    } finally {
                        MDC.remove("id")
                    }
                }
            }
        } else if (method.isProcedureQuery) {
            object : ProcedureExecution() {
                override fun doExecute(
                        jpaQuery: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return try {
                        MDC.put("id", sqlLogId)
                        super.doExecute(jpaQuery, accessor)
                    } finally {
                        MDC.remove("id")
                    }
                }
            }
        } else if (method.isStreamQuery) {
            object : StreamExecution() {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return try {
                        MDC.put("id", sqlLogId)
                        super.doExecute(query, accessor)
                    } finally {
                        MDC.remove("id")
                    }
                }
            }
        } else if (method.isSliceQuery) {
            object : SlicedExecution() {
                override fun doExecute(
                        query: AbstractJpaQuery,
                        accessor: JpaParametersParameterAccessor
                ): Any {
                    return try {
                        MDC.put("id", sqlLogId)
                        val pageable = accessor.pageable
                        if (pageable.isPaged && nestedResultMapType != null) {
                            if (nestedResultMapType!!.isCollection) {
                                throw UnsupportedOperationException(nestedResultMapType!!.nestedResultMapId
                                        + " collection resultmap not support page query")
                            } else {
                                sqlLog.info(
                                        "{} may return incorrect paginated data. Please check result maps definition {}.",
                                        sqlLogId, nestedResultMapType?.nestedResultMapId)
                            }
                        }
                        val result = super.doExecute(query, accessor) as SliceImpl<*>
                        if (sqlLog.isDebugEnabled) {
                            sqlLog.debug("total: {} rows", result.numberOfElements)
                            sqlLog.debug("{} rows retrieved", result.size)
                        }
                        result
                    } finally {
                        MDC.remove("id")
                    }
                }
            }
        } else {
            object : SingleEntityExecution() {
                @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE")
                override fun doExecute(query: AbstractJpaQuery, accessor: JpaParametersParameterAccessor): Any? {
                    return try {
                        MDC.put("id", sqlLogId)
                        val result: Any? = super.doExecute(query, accessor)
                        if (sqlLog.isDebugEnabled) {
                            sqlLog.debug("{} rows retrieved", if (result == null) 0 else 1)
                        }
                        result
                    } finally {
                        MDC.remove("id")
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
