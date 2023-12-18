package top.bettercode.summer.data.jpa.query.mybatis.hibernate

import org.hibernate.LockOptions
import org.hibernate.cache.spi.QueryKey
import org.hibernate.internal.util.collections.ArrayHelper
import org.hibernate.query.TupleTransformer
import org.hibernate.sql.exec.SqlExecLogger
import org.hibernate.sql.exec.internal.JdbcExecHelper
import org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl
import org.hibernate.sql.exec.spi.ExecutionContext
import org.hibernate.sql.exec.spi.JdbcOperationQuerySelect
import org.hibernate.sql.exec.spi.JdbcParameterBindings
import org.hibernate.sql.results.internal.ResultsHelper
import org.hibernate.sql.results.internal.RowProcessingStateStandardImpl
import org.hibernate.sql.results.internal.RowTransformerStandardImpl
import org.hibernate.sql.results.internal.RowTransformerTupleTransformerAdapter
import org.hibernate.sql.results.jdbc.internal.*
import org.hibernate.sql.results.jdbc.spi.JdbcValues
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMapping
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMetadata
import org.hibernate.sql.results.jdbc.spi.JdbcValuesSourceProcessingOptions
import org.hibernate.sql.results.spi.ResultsConsumer
import org.hibernate.sql.results.spi.RowTransformer
import org.hibernate.type.BasicType
import org.hibernate.type.descriptor.java.JavaType
import org.hibernate.type.spi.TypeConfiguration
import org.slf4j.LoggerFactory
import sun.misc.Unsafe
import top.bettercode.summer.data.jpa.query.mybatis.MybatisResultTransformer
import java.io.Serializable
import java.sql.PreparedStatement
import java.util.concurrent.TimeUnit
import java.util.function.Function

/**
 *
 * @author Peter Wu
 */
class JdbcExtSelectExecutorStandardImpl : JdbcSelectExecutorStandardImpl() {

    override fun <T, R> executeQuery(
        jdbcSelect: JdbcOperationQuerySelect,
        jdbcParameterBindings: JdbcParameterBindings,
        executionContext: ExecutionContext,
        rowTransformer: RowTransformer<R>?,
        domainResultType: Class<R>?,
        statementCreator: Function<String, PreparedStatement>,
        resultsConsumer: ResultsConsumer<T, R>
    ): T {
        val persistenceContext = executionContext.session.persistenceContext
        val defaultReadOnlyOrig = persistenceContext.isDefaultReadOnly
        val readOnly = executionContext.queryOptions.isReadOnly
        if (readOnly != null) {
            // The read-only/modifiable mode for the query was explicitly set.
            // Temporarily set the default read-only/modifiable setting to the query's setting.
            persistenceContext.isDefaultReadOnly = readOnly
        }
        try {
            return doExecuteQuery(
                jdbcSelect,
                jdbcParameterBindings,
                executionContext,
                rowTransformer,
                domainResultType,
                statementCreator,
                resultsConsumer
            )
        } finally {
            if (readOnly != null) {
                persistenceContext.isDefaultReadOnly = defaultReadOnlyOrig
            }
        }
    }

    private fun <T, R> doExecuteQuery(
        jdbcSelect: JdbcOperationQuerySelect,
        jdbcParameterBindings: JdbcParameterBindings,
        executionContext: ExecutionContext,
        rowTransformer: RowTransformer<R>?,
        domainResultType: Class<R>?,
        statementCreator: Function<String, PreparedStatement>,
        resultsConsumer: ResultsConsumer<T, R>
    ): T {
        var rowTransformer1: RowTransformer<R>? = rowTransformer
        val deferredResultSetAccess = DeferredResultSetAccess(
            jdbcSelect,
            jdbcParameterBindings,
            executionContext,
            statementCreator
        )
        val jdbcValues = resolveJdbcValuesSource(
            executionContext.getQueryIdentifier(deferredResultSetAccess.finalSql),
            jdbcSelect,
            resultsConsumer.canResultsBeCached(),
            executionContext,
            deferredResultSetAccess
        )

        val tupleTransformer: TupleTransformer<R>?
        if (rowTransformer1 == null) {
            @Suppress("UNCHECKED_CAST")
            tupleTransformer =
                executionContext.queryOptions.tupleTransformer as TupleTransformer<R>?

            if (tupleTransformer == null) {
                rowTransformer1 = RowTransformerStandardImpl.instance()
            } else {
                val domainResults = jdbcValues.valuesMapping.domainResults
                val aliases = arrayOfNulls<String>(domainResults.size)
                for (i in domainResults.indices) {
                    aliases[i] = domainResults[i].resultVariable
                }
                rowTransformer1 = RowTransformerTupleTransformerAdapter(aliases, tupleTransformer)
            }
        } else {
            tupleTransformer = null
        }

        val session = executionContext.session

        val stats: Boolean
        var startTime: Long = 0
        val statistics = session.factory.statistics
        if (executionContext.hasQueryExecutionToBeAddedToStatistics()
            && jdbcValues is JdbcValuesResultSetImpl
        ) {
            stats = statistics.isStatisticsEnabled
            if (stats) {
                startTime = System.nanoTime()
            }
        } else {
            stats = false
        }
        val result: T = if (tupleTransformer is MybatisResultTransformer) {
            val list = (tupleTransformer as MybatisResultTransformer).transformListResultSet(
                deferredResultSetAccess.resultSet
            )
            if (executionContext.isScrollResult) {
                @Suppress("UNCHECKED_CAST")
                MybatisScrollableResultsImplementor(list) as T
            } else {
                @Suppress("UNCHECKED_CAST")
                list as T
            }
        } else {
            /*
		 * Processing options effectively are only used for entity loading.  Here we don't need these values.
		 */
            val processingOptions: JdbcValuesSourceProcessingOptions =
                object : JdbcValuesSourceProcessingOptions {
                    override fun getEffectiveOptionalObject(): Any? {
                        return executionContext.entityInstance
                    }

                    override fun getEffectiveOptionalEntityName(): String? {
                        return null
                    }

                    override fun getEffectiveOptionalId(): Any? {
                        return executionContext.entityId
                    }

                    override fun shouldReturnProxies(): Boolean {
                        return true
                    }
                }

            val valuesProcessingState = JdbcValuesSourceProcessingStateStandardImpl(
                executionContext,
                processingOptions
            )

            val rowReader = ResultsHelper.createRowReader(
                executionContext,  // If follow-on locking is used, we must omit the lock options here,
                // because these lock options are only for Initializers.
                // If we wouldn't omit this, the follow-on lock requests would be no-ops,
                // because the EntityEntrys would already have the desired lock mode
                if (deferredResultSetAccess.usesFollowOnLocking()
                ) LockOptions.NONE
                else executionContext.queryOptions.lockOptions,
                rowTransformer1,
                domainResultType,
                jdbcValues
            )

            val rowProcessingState = RowProcessingStateStandardImpl(
                valuesProcessingState,
                executionContext,
                rowReader,
                jdbcValues
            )

            resultsConsumer.consume(
                jdbcValues,
                session,
                processingOptions,
                valuesProcessingState,
                rowProcessingState,
                rowReader
            )
        }
        if (stats) {
            val endTime = System.nanoTime()
            val milliseconds =
                TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS)
            statistics.queryExecuted(
                executionContext.getQueryIdentifier(jdbcSelect.sqlString),
                getResultSize(result),
                milliseconds
            )
        }

        return result
    }

    private fun <T> getResultSize(result: T): Int {
        if (result is List<*>) {
            return (result as List<*>).size
        }
        return -1
    }

    override fun resolveJdbcValuesSource(
        queryIdentifier: String?,
        jdbcSelect: JdbcOperationQuerySelect,
        canBeCached: Boolean,
        executionContext: ExecutionContext,
        resultSetAccess: ResultSetAccess
    ): JdbcValues {
        val session = executionContext.session
        val factory = session.factory
        val queryCacheEnabled = factory.sessionFactoryOptions.isQueryCacheEnabled

        val cachedResults: List<*>?
        val cacheMode = JdbcExecHelper.resolveCacheMode(executionContext)

        val mappingProducer = jdbcSelect.jdbcValuesMappingProducer
        val cacheable =
            queryCacheEnabled && canBeCached && executionContext.queryOptions.isResultCachingEnabled == true
        val queryResultsCacheKey: QueryKey?

        if (cacheable && cacheMode.isGetEnabled) {
            SqlExecLogger.SQL_EXEC_LOGGER.debugf(
                "Reading Query result cache data per CacheMode#isGetEnabled [%s]",
                cacheMode.name
            )
            val querySpaces = jdbcSelect.affectedTableNames
            if (querySpaces == null || querySpaces.size == 0) {
                SqlExecLogger.SQL_EXEC_LOGGER.tracef("Unexpected querySpaces is empty")
            } else {
                SqlExecLogger.SQL_EXEC_LOGGER.tracef("querySpaces is `%s`", querySpaces)
            }

            val queryCache = factory.cache
                .getQueryResultsCache(executionContext.queryOptions.resultCacheRegionName)

            queryResultsCacheKey = QueryKey.from(
                jdbcSelect.sqlString,
                executionContext.queryOptions.limit,
                executionContext.queryParameterBindings,
                session
            )

            cachedResults = queryCache[queryResultsCacheKey, querySpaces, session]

            //  (6.0) : `querySpaces` and `session` are used in QueryCache#get to verify "up-to-dateness" via UpdateTimestampsCache
            //		better imo to move UpdateTimestampsCache handling here and have QueryCache be a simple access to
            //		the underlying query result cache region.
            //
            //  (6.0) : if we go this route (^^), still beneficial to have an abstraction over different UpdateTimestampsCache-based
            //		invalidation strategies - QueryCacheInvalidationStrategy
            val statistics = factory.statistics
            if (statistics.isStatisticsEnabled) {
                if (cachedResults == null) {
                    statistics.queryCacheMiss(queryIdentifier!!, queryCache.region.name)
                } else {
                    statistics.queryCacheHit(queryIdentifier!!, queryCache.region.name)
                }
            }
        } else {
            SqlExecLogger.SQL_EXEC_LOGGER.debugf(
                "Skipping reading Query result cache data: cache-enabled = %s, cache-mode = %s",
                queryCacheEnabled,
                cacheMode.name
            )
            cachedResults = null
            queryResultsCacheKey = if (cacheable && cacheMode.isPutEnabled) {
                QueryKey.from(
                    jdbcSelect.sqlString,
                    executionContext.queryOptions.limit,
                    executionContext.queryParameterBindings,
                    session
                )
            } else {
                null
            }
        }

        if (cachedResults == null) {
            val metadataForCache: JdbcValuesMetadata?
            val jdbcValuesMapping: JdbcValuesMapping
            if (queryResultsCacheKey == null) {
                jdbcValuesMapping =
                    mappingProducer.resolve(resultSetAccess, session.loadQueryInfluencers, factory)
                metadataForCache = null
            } else {
                // If we need to put the values into the cache, we need to be able to capture the JdbcValuesMetadata
                val capturingMetadata = CapturingJdbcValuesMetadata(resultSetAccess)
                jdbcValuesMapping = mappingProducer.resolve(
                    capturingMetadata,
                    session.loadQueryInfluencers,
                    factory
                )
                metadataForCache = capturingMetadata.resolveMetadataForCache()
            }

            return JdbcValuesResultSetImpl(
                resultSetAccess,
                queryResultsCacheKey,
                queryIdentifier,
                executionContext.queryOptions,
                jdbcValuesMapping,
                metadataForCache,
                executionContext
            )
        } else {
            val jdbcValuesMapping =
                if (cachedResults.isEmpty() || cachedResults[0] !is JdbcValuesMetadata) {
                    mappingProducer.resolve(resultSetAccess, session.loadQueryInfluencers, factory)
                } else {
                    mappingProducer.resolve(
                        cachedResults[0] as JdbcValuesMetadata?,
                        session.loadQueryInfluencers,
                        factory
                    )
                }
            return JdbcValuesCacheHit(cachedResults, jdbcValuesMapping)
        }
    }

    private class CapturingJdbcValuesMetadata(private val resultSetAccess: ResultSetAccess) :
        JdbcValuesMetadata {
        private var columnNames: Array<String?>? = null
        private var types: Array<BasicType<*>?>? = null

        private fun initializeArrays() {
            val columnCount = resultSetAccess.columnCount
            columnNames = arrayOfNulls(columnCount)
            types = arrayOfNulls(columnCount)
        }

        override fun getColumnCount(): Int {
            if (columnNames == null) {
                initializeArrays()
            }
            return columnNames!!.size
        }

        override fun resolveColumnPosition(columnName: String): Int {
            if (columnNames == null) {
                initializeArrays()
            }
            var position: Int
            if (columnNames == null) {
                position = resultSetAccess.resolveColumnPosition(columnName)
                columnNames!![position - 1] = columnName
            } else if (((ArrayHelper.indexOf(columnNames, columnName) + 1).also {
                    position = it
                }) == 0) {
                position = resultSetAccess.resolveColumnPosition(columnName)
                columnNames!![position - 1] = columnName
            }
            return position
        }

        override fun resolveColumnName(position: Int): String? {
            if (columnNames == null) {
                initializeArrays()
            }
            var name: String? = null
            if (columnNames == null) {
                name = resultSetAccess.resolveColumnName(position)
                columnNames!![position - 1] = name
            } else if ((columnNames!![position - 1]?.also { name = it }) == null) {
                name = resultSetAccess.resolveColumnName(position)
                columnNames!![position - 1] = name
            }
            return name
        }

        override fun <J> resolveType(
            position: Int,
            explicitJavaType: JavaType<J>,
            typeConfiguration: TypeConfiguration
        ): BasicType<J> {
            if (columnNames == null) {
                initializeArrays()
            }
            val basicType = resultSetAccess.resolveType(
                position,
                explicitJavaType,
                typeConfiguration
            )
            types!![position - 1] = basicType
            return basicType
        }

        fun resolveMetadataForCache(): JdbcValuesMetadata? {
            if (columnNames == null) {
                return null
            }
            return CachedJdbcValuesMetadata(columnNames!!, types!!)
        }
    }

    private class CachedJdbcValuesMetadata(
        private val columnNames: Array<String?>, private val types: Array<BasicType<*>?>
    ) :
        JdbcValuesMetadata, Serializable {
        override fun getColumnCount(): Int {
            return columnNames.size
        }

        override fun resolveColumnPosition(columnName: String): Int {
            val position = ArrayHelper.indexOf(
                columnNames, columnName
            ) + 1
            check(position != 0) { "Unexpected resolving of unavailable column: $columnName" }
            return position
        }

        override fun resolveColumnName(position: Int): String {
            val name = columnNames[position - 1]
                ?: throw IllegalStateException("Unexpected resolving of unavailable column at position: $position")
            return name
        }

        override fun <J> resolveType(
            position: Int,
            explicitJavaType: JavaType<J>?,
            typeConfiguration: TypeConfiguration
        ): BasicType<J> {
            val type = types[position - 1]
                ?: throw IllegalStateException("Unexpected resolving of unavailable column at position: $position")
            return if (explicitJavaType == null || type.javaTypeDescriptor === explicitJavaType) {
                @Suppress("UNCHECKED_CAST")
                type as BasicType<J>
            } else {
                typeConfiguration.basicTypeRegistry.resolve(
                    explicitJavaType,
                    type.jdbcType
                )
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JdbcExtSelectExecutorStandardImpl::class.java)

        @JvmStatic
        fun changeInstance() {
            try {
                val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
                unsafeField.isAccessible = true
                val unsafe = unsafeField[null] as Unsafe
                val field = JdbcSelectExecutorStandardImpl::class.java.getDeclaredField("INSTANCE")
                val fieldBase = unsafe.staticFieldBase(field)
                val fieldOffset = unsafe.staticFieldOffset(field)
                unsafe.putObject(fieldBase, fieldOffset, JdbcExtSelectExecutorStandardImpl())
                log.info("替换JdbcSelectExecutor")
            } catch (e: NoSuchFieldException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
        }
    }

}
