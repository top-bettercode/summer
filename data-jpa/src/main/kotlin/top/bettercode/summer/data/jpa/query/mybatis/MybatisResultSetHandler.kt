package top.bettercode.summer.data.jpa.query.mybatis

import org.apache.ibatis.annotations.AutomapConstructor
import org.apache.ibatis.cache.CacheKey
import org.apache.ibatis.executor.ErrorContext
import org.apache.ibatis.executor.ExecutorException
import org.apache.ibatis.executor.loader.ResultLoaderMap
import org.apache.ibatis.executor.result.DefaultResultContext
import org.apache.ibatis.executor.result.DefaultResultHandler
import org.apache.ibatis.executor.result.ResultMapException
import org.apache.ibatis.executor.resultset.ResultSetWrapper
import org.apache.ibatis.mapping.*
import org.apache.ibatis.reflection.MetaClass
import org.apache.ibatis.reflection.MetaObject
import org.apache.ibatis.reflection.ReflectorFactory
import org.apache.ibatis.reflection.factory.ObjectFactory
import org.apache.ibatis.session.*
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.TypeHandler
import org.apache.ibatis.type.TypeHandlerRegistry
import org.apache.ibatis.util.MapUtil
import java.lang.reflect.Constructor
import java.sql.ResultSet
import java.sql.SQLException

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Iwao AVE!
 * @author Kazuki Shimizu
 */
open class MybatisResultSetHandler @JvmOverloads constructor(private val mappedStatement: MappedStatement?, private val resultHandler: ResultHandler<Any?>? = null) {
    private val configuration: Configuration = mappedStatement!!.configuration
    private val typeHandlerRegistry: TypeHandlerRegistry = configuration.typeHandlerRegistry
    private val objectFactory: ObjectFactory = configuration.objectFactory
    private val reflectorFactory: ReflectorFactory = configuration.reflectorFactory

    // nested resultmaps
    private val nestedResultObjects: MutableMap<CacheKey, Any?> = HashMap()
    private val ancestorObjects: MutableMap<String, Any> = HashMap()
    private var previousRowValue: Any? = null

    // multiple resultsets
    private val nextResultMaps: MutableMap<String, ResultMapping> = HashMap()
    private val pendingRelations: Map<CacheKey, MutableList<PendingRelation>> = HashMap()

    // Cached Automappings
    private val autoMappingsCache: MutableMap<String, MutableList<UnMappedColumnAutoMapping>> = HashMap()

    // temporary marking flag that indicate using constructor mapping (use field to reduce memory usage)
    private var useConstructorMappings = false

    private class PendingRelation {
        var metaObject: MetaObject? = null
        var propertyMapping: ResultMapping? = null
    }

    private class UnMappedColumnAutoMapping(
            val column: String, val property: String, val typeHandler: TypeHandler<*>,
            val primitive: Boolean
    )

    //
    // HANDLE RESULT SETS
    //
    @Throws(SQLException::class)
    fun handleResultSets(resultSet: ResultSet?, maxRows: Int): List<*> {
        ErrorContext.instance().activity("handling results").`object`(mappedStatement!!.id)
        val multipleResults: MutableList<Any> = ArrayList()
        val rsw = getResultSet(resultSet)
        val rowBounds = RowBounds(0, maxRows)
        val resultMaps = mappedStatement.resultMaps
        if (resultMaps.size < 1) {
            throw ExecutorException(
                    "A query was run and no Result Maps were found for the Mapped Statement '"
                            + mappedStatement.id
                            + "'.  It's likely that neither a Result Type nor a Result Map was specified.")
        }
        val resultMap = resultMaps[0]
        handleResultSet(rsw, resultMap, multipleResults, null, rowBounds)
        cleanUpAfterHandlingResultSet()
        return collapseSingleResultList(multipleResults)
    }

    @Throws(SQLException::class)
    private fun getResultSet(rs: ResultSet?): ResultSetWrapper? {
        return if (rs != null) ResultSetWrapper(rs, configuration) else null
    }

    private fun closeResultSet(rs: ResultSet?) {
        try {
            rs?.close()
        } catch (e: SQLException) {
            // ignore
        }
    }

    private fun cleanUpAfterHandlingResultSet() {
        nestedResultObjects.clear()
    }

    @Throws(SQLException::class)
    private fun handleResultSet(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            multipleResults: MutableList<Any>, parentMapping: ResultMapping?, rowBounds: RowBounds
    ) {
        try {
            if (parentMapping != null) {
                handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping)
            } else {
                if (resultHandler == null) {
                    val defaultResultHandler = DefaultResultHandler(objectFactory)
                    handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null)
                    multipleResults.add(defaultResultHandler.resultList)
                } else {
                    handleRowValues(rsw, resultMap, resultHandler, rowBounds, null)
                }
            }
        } finally {
            // issue #228 (close resultsets)
            closeResultSet(rsw!!.resultSet)
        }
    }

    private fun collapseSingleResultList(multipleResults: List<*>): List<*> {
        return if (multipleResults.size == 1) multipleResults[0] as List<*> else multipleResults
    }

    //
    // HANDLE ROWS FOR SIMPLE RESULTMAP
    //
    @Throws(SQLException::class)
    fun handleRowValues(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            resultHandler: ResultHandler<Any?>?, rowBounds: RowBounds, parentMapping: ResultMapping?
    ) {
        if (resultMap.hasNestedResultMaps()) {
            ensureNoRowBounds(rowBounds)
            checkResultHandler()
            handleRowValuesForNestedResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping)
        } else {
            handleRowValuesForSimpleResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping)
        }
    }

    private fun ensureNoRowBounds(rowBounds: RowBounds?) {
        if (configuration.isSafeRowBoundsEnabled && rowBounds != null && (rowBounds.limit < RowBounds.NO_ROW_LIMIT
                        || rowBounds.offset > RowBounds.NO_ROW_OFFSET)) {
            throw ExecutorException("Mapped Statements with nested result mappings cannot be safely constrained by RowBounds. "
                    + "Use safeRowBoundsEnabled=false setting to bypass this check.")
        }
    }

    protected fun checkResultHandler() {
        if (resultHandler != null && configuration.isSafeResultHandlerEnabled
                && !mappedStatement!!.isResultOrdered) {
            throw ExecutorException(
                    "Mapped Statements with nested result mappings cannot be safely used with a custom ResultHandler. "
                            + "Use safeResultHandlerEnabled=false setting to bypass this check "
                            + "or ensure your statement returns ordered data and set resultOrdered=true on it.")
        }
    }

    @Throws(SQLException::class)
    private fun handleRowValuesForSimpleResultMap(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            resultHandler: ResultHandler<Any?>?, rowBounds: RowBounds, parentMapping: ResultMapping?
    ) {
        val resultContext = DefaultResultContext<Any?>()
        val resultSet = rsw!!.resultSet
        skipRows(resultSet, rowBounds)
        while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed
                && resultSet.next()) {
            val discriminatedResultMap = resolveDiscriminatedResultMap(resultSet, resultMap, null)
            val rowValue = getRowValue(rsw, discriminatedResultMap, null)
            storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet)
        }
    }

    @Throws(SQLException::class)
    private fun storeObject(
            resultHandler: ResultHandler<Any?>?,
            resultContext: DefaultResultContext<Any?>, rowValue: Any?, parentMapping: ResultMapping?,
            rs: ResultSet
    ) {
        if (parentMapping != null) {
            linkToParents(rs, parentMapping, rowValue)
        } else {
            callResultHandler(resultHandler, resultContext, rowValue)
        }
    }

    private fun callResultHandler(
            resultHandler: ResultHandler<Any?>?,
            resultContext: DefaultResultContext<Any?>, rowValue: Any?
    ) {
        resultContext.nextResultObject(rowValue)
        resultHandler?.handleResult(resultContext)
    }

    private fun shouldProcessMoreRows(context: ResultContext<*>, rowBounds: RowBounds): Boolean {
        return !context.isStopped && context.resultCount < rowBounds.limit
    }

    @Throws(SQLException::class)
    private fun skipRows(rs: ResultSet, rowBounds: RowBounds) {
        if (rs.type != ResultSet.TYPE_FORWARD_ONLY) {
            if (rowBounds.offset != RowBounds.NO_ROW_OFFSET) {
                rs.absolute(rowBounds.offset)
            }
        } else {
            for (i in 0 until rowBounds.offset) {
                if (!rs.next()) {
                    break
                }
            }
        }
    }

    //
    // GET VALUE FROM ROW FOR SIMPLE RESULT MAP
    //
    @Throws(SQLException::class)
    private fun getRowValue(rsw: ResultSetWrapper?, resultMap: ResultMap, columnPrefix: String?): Any? {
        val lazyLoader = ResultLoaderMap()
        var rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix)
        if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.type)) {
            val metaObject = configuration.newMetaObject(rowValue)
            var foundValues = useConstructorMappings
            if (shouldApplyAutomaticMappings(resultMap, false)) {
                foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues
            }
            foundValues = (applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix)
                    || foundValues)
            foundValues = lazyLoader.size() > 0 || foundValues
            rowValue = if (foundValues || configuration.isReturnInstanceForEmptyRow) rowValue else null
        }
        return rowValue
    }

    //
    // GET VALUE FROM ROW FOR NESTED RESULT MAP
    //
    @Throws(SQLException::class)
    private fun getRowValue(
            rsw: ResultSetWrapper?, resultMap: ResultMap, combinedKey: CacheKey,
            columnPrefix: String?, partialObject: Any?
    ): Any? {
        val resultMapId = resultMap.id
        var rowValue = partialObject
        if (rowValue != null) {
            val metaObject = configuration.newMetaObject(rowValue)
            putAncestor(rowValue, resultMapId)
            applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combinedKey, false)
            ancestorObjects.remove(resultMapId)
        } else {
            val lazyLoader = ResultLoaderMap()
            rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix)
            if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.type)) {
                val metaObject = configuration.newMetaObject(rowValue)
                var foundValues = useConstructorMappings
                if (shouldApplyAutomaticMappings(resultMap, true)) {
                    foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues
                }
                foundValues = (applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix)
                        || foundValues)
                putAncestor(rowValue, resultMapId)
                foundValues = (applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combinedKey, true)
                        || foundValues)
                ancestorObjects.remove(resultMapId)
                foundValues = lazyLoader.size() > 0 || foundValues
                rowValue = if (foundValues || configuration.isReturnInstanceForEmptyRow) rowValue else null
            }
            if (combinedKey !== CacheKey.NULL_CACHE_KEY) {
                nestedResultObjects[combinedKey] = rowValue
            }
        }
        return rowValue
    }

    private fun putAncestor(resultObject: Any, resultMapId: String) {
        ancestorObjects[resultMapId] = resultObject
    }

    private fun shouldApplyAutomaticMappings(resultMap: ResultMap, isNested: Boolean): Boolean {
        return if (resultMap.autoMapping != null) {
            resultMap.autoMapping
        } else {
            if (isNested) {
                AutoMappingBehavior.FULL == configuration.autoMappingBehavior
            } else {
                AutoMappingBehavior.NONE != configuration.autoMappingBehavior
            }
        }
    }

    //
    // PROPERTY MAPPINGS
    //
    @Throws(SQLException::class)
    private fun applyPropertyMappings(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            metaObject: MetaObject, lazyLoader: ResultLoaderMap, columnPrefix: String?
    ): Boolean {
        val mappedColumnNames = rsw!!.getMappedColumnNames(resultMap, columnPrefix)
        var foundValues = false
        val propertyMappings = resultMap.propertyResultMappings
        for (propertyMapping in propertyMappings) {
            var column = prependPrefix(propertyMapping.column, columnPrefix)
            if (propertyMapping.nestedResultMapId != null) {
                // the user added a column attribute to a nested result map, ignore it
                column = null
            }
            if (propertyMapping.isCompositeResult || column != null && mappedColumnNames.contains(column.uppercase()) || propertyMapping.resultSet != null) {
                val value = getPropertyMappingValue(rsw.resultSet, metaObject, propertyMapping,
                        lazyLoader, columnPrefix)
                // issue #541 make property optional
                val property = propertyMapping.property
                if (property == null) {
                    continue
                } else if (value === DEFERRED) {
                    foundValues = true
                    continue
                }
                if (value != null) {
                    foundValues = true
                }
                if (value != null || configuration.isCallSettersOnNulls && !metaObject.getSetterType(
                                property).isPrimitive) {
                    // gcode issue #377, call setter on nulls (value is not 'found')
                    metaObject.setValue(property, value)
                }
            }
        }
        return foundValues
    }

    @Throws(SQLException::class)
    private fun getPropertyMappingValue(
            rs: ResultSet, metaResultObject: MetaObject,
            propertyMapping: ResultMapping, @Suppress("UNUSED_PARAMETER") lazyLoader: ResultLoaderMap, columnPrefix: String?
    ): Any? {
        return if (propertyMapping.nestedQueryId != null) {
            throw UnsupportedOperationException()
        } else if (propertyMapping.resultSet != null) {
            addPendingChildRelation(rs, metaResultObject, propertyMapping)
            DEFERRED
        } else {
            val typeHandler = propertyMapping.typeHandler
            val column = prependPrefix(propertyMapping.column, columnPrefix)
            typeHandler.getResult(rs, column)
        }
    }

    @Throws(SQLException::class)
    private fun createAutomaticMappings(
            rsw: ResultSetWrapper?,
            resultMap: ResultMap, metaObject: MetaObject, columnPrefix: String?
    ): List<UnMappedColumnAutoMapping> {
        val mapKey = resultMap.id + ":" + columnPrefix
        var autoMapping = autoMappingsCache[mapKey]
        if (autoMapping == null) {
            autoMapping = ArrayList()
            val unmappedColumnNames = rsw!!.getUnmappedColumnNames(resultMap, columnPrefix)
            for (columnName in unmappedColumnNames) {
                var propertyName = columnName
                if (!columnPrefix.isNullOrEmpty()) {
                    // When columnPrefix is specified,
                    // ignore columns without the prefix.
                    propertyName = if (columnName.uppercase().startsWith(columnPrefix)) {
                        columnName.substring(columnPrefix.length)
                    } else {
                        continue
                    }
                }
                val property = metaObject.findProperty(propertyName,
                        configuration.isMapUnderscoreToCamelCase)
                if (property != null && metaObject.hasSetter(property)) {
                    if (resultMap.mappedProperties.contains(property)) {
                        continue
                    }
                    val propertyType = metaObject.getSetterType(property)
                    if (typeHandlerRegistry.hasTypeHandler(propertyType, rsw.getJdbcType(columnName))) {
                        val typeHandler = rsw.getTypeHandler(propertyType, columnName)
                        autoMapping.add(UnMappedColumnAutoMapping(columnName, property, typeHandler,
                                propertyType.isPrimitive))
                    } else {
                        configuration.autoMappingUnknownColumnBehavior
                                .doAction(mappedStatement, columnName, property, propertyType)
                    }
                } else {
                    configuration.autoMappingUnknownColumnBehavior
                            .doAction(mappedStatement, columnName, property ?: propertyName,
                                    null)
                }
            }
            autoMappingsCache[mapKey] = autoMapping
        }
        return autoMapping
    }

    @Throws(SQLException::class)
    private fun applyAutomaticMappings(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            metaObject: MetaObject, columnPrefix: String?
    ): Boolean {
        val autoMapping = createAutomaticMappings(rsw, resultMap,
                metaObject, columnPrefix)
        var foundValues = false
        if (autoMapping.isNotEmpty()) {
            for (mapping in autoMapping) {
                val value = mapping.typeHandler.getResult(rsw!!.resultSet, mapping.column)
                if (value != null) {
                    foundValues = true
                }
                if (value != null || configuration.isCallSettersOnNulls && !mapping.primitive) {
                    // gcode issue #377, call setter on nulls (value is not 'found')
                    metaObject.setValue(mapping.property, value)
                }
            }
        }
        return foundValues
    }

    // MULTIPLE RESULT SETS
    @Throws(SQLException::class)
    private fun linkToParents(rs: ResultSet, parentMapping: ResultMapping, rowValue: Any?) {
        val parentKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.column,
                parentMapping.foreignColumn)
        val parents: List<PendingRelation?>? = pendingRelations[parentKey]
        if (parents != null) {
            for (parent in parents) {
                if (parent != null && rowValue != null) {
                    linkObjects(parent.metaObject, parent.propertyMapping, rowValue)
                }
            }
        }
    }

    @Throws(SQLException::class)
    private fun addPendingChildRelation(
            rs: ResultSet, metaResultObject: MetaObject,
            parentMapping: ResultMapping
    ) {
        val cacheKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.column,
                parentMapping.column)
        val deferLoad = PendingRelation()
        deferLoad.metaObject = metaResultObject
        deferLoad.propertyMapping = parentMapping
        val relations = MapUtil.computeIfAbsent(pendingRelations, cacheKey
        ) { _: CacheKey? -> ArrayList() }
        // issue #255
        relations.add(deferLoad)
        val previous = nextResultMaps[parentMapping.resultSet]
        if (previous == null) {
            nextResultMaps[parentMapping.resultSet] = parentMapping
        } else {
            if (previous != parentMapping) {
                throw ExecutorException("Two different properties are mapped to the same resultSet")
            }
        }
    }

    @Throws(SQLException::class)
    private fun createKeyForMultipleResults(
            rs: ResultSet, resultMapping: ResultMapping,
            names: String?, columns: String?
    ): CacheKey {
        val cacheKey = CacheKey()
        cacheKey.update(resultMapping)
        if (columns != null && names != null) {
            val columnsArray = columns.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val namesArray = names.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in columnsArray.indices) {
                val value: Any? = rs.getString(columnsArray[i])
                if (value != null) {
                    cacheKey.update(namesArray[i])
                    cacheKey.update(value)
                }
            }
        }
        return cacheKey
    }

    //
    // INSTANTIATION & CONSTRUCTOR MAPPING
    //
    @Throws(SQLException::class)
    private fun createResultObject(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            lazyLoader: ResultLoaderMap, columnPrefix: String?
    ): Any? {
        useConstructorMappings = false // reset previous mapping result
        val constructorArgTypes: MutableList<Class<*>> = ArrayList()
        val constructorArgs: MutableList<Any?> = ArrayList()
        var resultObject = createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs,
                columnPrefix)
        if (resultObject != null && !hasTypeHandlerForResultObject(rsw, resultMap.type)) {
            val propertyMappings = resultMap.propertyResultMappings
            for (propertyMapping in propertyMappings) {
                // issue gcode #109 && issue #149
                if (propertyMapping.nestedQueryId != null && propertyMapping.isLazy) {
                    resultObject = configuration.proxyFactory
                            .createProxy(resultObject, lazyLoader, configuration, objectFactory,
                                    constructorArgTypes, constructorArgs)
                    break
                }
            }
        }
        useConstructorMappings = resultObject != null && constructorArgTypes.isNotEmpty() // set current mapping result
        return resultObject
    }

    @Throws(SQLException::class)
    private fun createResultObject(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            constructorArgTypes: MutableList<Class<*>>, constructorArgs: MutableList<Any?>, columnPrefix: String?
    ): Any? {
        val resultType = resultMap.type
        val metaType = MetaClass.forClass(resultType, reflectorFactory)
        val constructorMappings = resultMap.constructorResultMappings
        if (hasTypeHandlerForResultObject(rsw, resultType)) {
            return createPrimitiveResultObject(rsw, resultMap, columnPrefix)
        } else if (constructorMappings.isNotEmpty()) {
            return createParameterizedResultObject(rsw, resultType, constructorMappings,
                    constructorArgTypes, constructorArgs, columnPrefix)
        } else if (resultType.isInterface || metaType.hasDefaultConstructor()) {
            return objectFactory.create(resultType)
        } else if (shouldApplyAutomaticMappings(resultMap, false)) {
            return createByConstructorSignature(rsw, resultType, constructorArgTypes, constructorArgs)
        }
        throw ExecutorException("Do not know how to create an instance of $resultType")
    }

    fun createParameterizedResultObject(
            rsw: ResultSetWrapper?, resultType: Class<*>?,
            constructorMappings: List<ResultMapping>,
            constructorArgTypes: MutableList<Class<*>>, constructorArgs: MutableList<Any?>, columnPrefix: String?
    ): Any? {
        var foundValues = false
        for (constructorMapping in constructorMappings) {
            val parameterType = constructorMapping.javaType
            val column = constructorMapping.column
            val value: Any? = try {
                if (constructorMapping.nestedQueryId != null) {
                    throw UnsupportedOperationException()
                } else if (constructorMapping.nestedResultMapId != null) {
                    val resultMap = configuration.getResultMap(
                            constructorMapping.nestedResultMapId)
                    getRowValue(rsw, resultMap, getColumnPrefix(columnPrefix, constructorMapping))
                } else {
                    val typeHandler = constructorMapping.typeHandler
                    typeHandler.getResult(rsw!!.resultSet, prependPrefix(column, columnPrefix))
                }
            } catch (e: ResultMapException) {
                throw ExecutorException("Could not process result for mapping: $constructorMapping",
                        e)
            } catch (e: SQLException) {
                throw ExecutorException("Could not process result for mapping: $constructorMapping",
                        e)
            }
            constructorArgTypes.add(parameterType)
            constructorArgs.add(value)
            foundValues = value != null || foundValues
        }
        return if (foundValues) objectFactory.create(resultType, constructorArgTypes, constructorArgs) else null
    }

    @Throws(SQLException::class)
    private fun createByConstructorSignature(
            rsw: ResultSetWrapper?, resultType: Class<*>,
            constructorArgTypes: MutableList<Class<*>>, constructorArgs: MutableList<Any?>
    ): Any? {
        val constructors = resultType.declaredConstructors
        val defaultConstructor = findDefaultConstructor(constructors)
        if (defaultConstructor != null) {
            return createUsingConstructor(rsw, resultType, constructorArgTypes, constructorArgs,
                    defaultConstructor)
        } else {
            for (constructor in constructors) {
                if (allowedConstructorUsingTypeHandlers(constructor, rsw!!.jdbcTypes)) {
                    return createUsingConstructor(rsw, resultType, constructorArgTypes, constructorArgs,
                            constructor)
                }
            }
        }
        throw ExecutorException(
                "No constructor found in " + resultType.name + " matching " + rsw!!.classNames)
    }

    @Throws(SQLException::class)
    private fun createUsingConstructor(
            rsw: ResultSetWrapper?, resultType: Class<*>,
            constructorArgTypes: MutableList<Class<*>>, constructorArgs: MutableList<Any?>, constructor: Constructor<*>
    ): Any? {
        var foundValues = false
        for (i in constructor.parameterTypes.indices) {
            val parameterType = constructor.parameterTypes[i]
            val columnName = rsw!!.columnNames[i]
            val typeHandler = rsw.getTypeHandler(parameterType, columnName)
            val value = typeHandler.getResult(rsw.resultSet, columnName)
            constructorArgTypes.add(parameterType)
            constructorArgs.add(value)
            foundValues = value != null || foundValues
        }
        return if (foundValues) objectFactory.create(resultType, constructorArgTypes, constructorArgs) else null
    }

    private fun findDefaultConstructor(constructors: Array<Constructor<*>>): Constructor<*>? {
        if (constructors.size == 1) {
            return constructors[0]
        }
        for (constructor in constructors) {
            if (constructor.isAnnotationPresent(AutomapConstructor::class.java)) {
                return constructor
            }
        }
        return null
    }

    private fun allowedConstructorUsingTypeHandlers(
            constructor: Constructor<*>,
            jdbcTypes: List<JdbcType>
    ): Boolean {
        val parameterTypes = constructor.parameterTypes
        if (parameterTypes.size != jdbcTypes.size) {
            return false
        }
        for (i in parameterTypes.indices) {
            if (!typeHandlerRegistry.hasTypeHandler(parameterTypes[i], jdbcTypes[i])) {
                return false
            }
        }
        return true
    }

    @Throws(SQLException::class)
    private fun createPrimitiveResultObject(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            columnPrefix: String?
    ): Any? {
        val resultType = resultMap.type
        val columnName: String? = if (resultMap.resultMappings.isNotEmpty()) {
            val resultMappingList = resultMap.resultMappings
            val mapping = resultMappingList[0]
            prependPrefix(mapping.column, columnPrefix)
        } else {
            rsw!!.columnNames[0]
        }
        val typeHandler = rsw!!.getTypeHandler(resultType, columnName)
        return typeHandler.getResult(rsw.resultSet, columnName)
    }

    //
    // DISCRIMINATOR
    //
    @Throws(SQLException::class)
    fun resolveDiscriminatedResultMap(
            rs: ResultSet, resultMap: ResultMap,
            columnPrefix: String?
    ): ResultMap {
        var resultMap1 = resultMap
        val pastDiscriminators: MutableSet<String> = HashSet()
        var discriminator = resultMap1.discriminator
        while (discriminator != null) {
            val value = getDiscriminatorValue(rs, discriminator, columnPrefix)
            val discriminatedMapId = discriminator.getMapIdFor(value.toString())
            if (configuration.hasResultMap(discriminatedMapId)) {
                resultMap1 = configuration.getResultMap(discriminatedMapId)
                val lastDiscriminator = discriminator
                discriminator = resultMap1.discriminator
                if (discriminator === lastDiscriminator || !pastDiscriminators.add(discriminatedMapId)) {
                    break
                }
            } else {
                break
            }
        }
        return resultMap1
    }

    @Throws(SQLException::class)
    private fun getDiscriminatorValue(
            rs: ResultSet, discriminator: Discriminator,
            columnPrefix: String?
    ): Any {
        val resultMapping = discriminator.resultMapping
        val typeHandler = resultMapping.typeHandler
        return typeHandler.getResult(rs, prependPrefix(resultMapping.column, columnPrefix))
    }

    private fun prependPrefix(columnName: String?, prefix: String?): String? {
        return if (columnName.isNullOrEmpty() || prefix.isNullOrEmpty()) {
            columnName
        } else prefix + columnName
    }

    //
    // HANDLE NESTED RESULT MAPS
    //
    @Throws(SQLException::class)
    private fun handleRowValuesForNestedResultMap(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            resultHandler: ResultHandler<Any?>?, rowBounds: RowBounds, parentMapping: ResultMapping?
    ) {
        val resultContext = DefaultResultContext<Any?>()
        val resultSet = rsw!!.resultSet
        skipRows(resultSet, rowBounds)
        var rowValue = previousRowValue
        while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed
                && resultSet.next()) {
            val discriminatedResultMap = resolveDiscriminatedResultMap(resultSet, resultMap,
                    null)
            val rowKey = createRowKey(discriminatedResultMap, rsw, null)
            val partialObject = nestedResultObjects[rowKey]
            // issue #577 && #542
            if (mappedStatement!!.isResultOrdered) {
                if (partialObject == null && rowValue != null) {
                    nestedResultObjects.clear()
                    storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet)
                }
                rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject)
            } else {
                rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject)
                if (partialObject == null) {
                    storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet)
                }
            }
        }
        if (rowValue != null && mappedStatement!!.isResultOrdered && shouldProcessMoreRows(
                        resultContext, rowBounds)) {
            storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet)
            previousRowValue = null
        } else if (rowValue != null) {
            previousRowValue = rowValue
        }
    }

    //
    // NESTED RESULT MAP (JOIN MAPPING)
    //
    private fun applyNestedResultMappings(
            rsw: ResultSetWrapper?, resultMap: ResultMap,
            metaObject: MetaObject, parentPrefix: String?, parentRowKey: CacheKey, newObject: Boolean
    ): Boolean {
        var foundValues = false
        for (resultMapping in resultMap.propertyResultMappings) {
            val nestedResultMapId = resultMapping.nestedResultMapId
            if (nestedResultMapId != null && resultMapping.resultSet == null) {
                try {
                    val columnPrefix = getColumnPrefix(parentPrefix, resultMapping)
                    val nestedResultMap = getNestedResultMap(rsw!!.resultSet,
                            nestedResultMapId, columnPrefix)
                    if (resultMapping.columnPrefix == null) {
                        // try to fill circular reference only when columnPrefix
                        // is not specified for the nested result map (issue #215)
                        val ancestorObject = ancestorObjects[nestedResultMapId]
                        if (ancestorObject != null) {
                            if (newObject) {
                                linkObjects(metaObject, resultMapping, ancestorObject) // issue #385
                            }
                            continue
                        }
                    }
                    val rowKey = createRowKey(nestedResultMap, rsw, columnPrefix)
                    val combinedKey = combineKeys(rowKey, parentRowKey)
                    var rowValue = nestedResultObjects[combinedKey]
                    val knownValue = rowValue != null
                    instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject) // mandatory
                    if (anyNotNullColumnHasValue(resultMapping, columnPrefix, rsw)) {
                        rowValue = getRowValue(rsw, nestedResultMap, combinedKey, columnPrefix, rowValue)
                        if (rowValue != null && !knownValue) {
                            linkObjects(metaObject, resultMapping, rowValue)
                            foundValues = true
                        }
                    }
                } catch (e: SQLException) {
                    throw ExecutorException(
                            "Error getting nested result map values for '" + resultMapping.property
                                    + "'.  Cause: " + e, e)
                }
            }
        }
        return foundValues
    }

    private fun getColumnPrefix(parentPrefix: String?, resultMapping: ResultMapping): String? {
        val columnPrefixBuilder = StringBuilder()
        if (parentPrefix != null) {
            columnPrefixBuilder.append(parentPrefix)
        }
        if (resultMapping.columnPrefix != null) {
            columnPrefixBuilder.append(resultMapping.columnPrefix)
        }
        return if (columnPrefixBuilder.isEmpty()) null else columnPrefixBuilder.toString().uppercase()
    }

    @Throws(SQLException::class)
    private fun anyNotNullColumnHasValue(
            resultMapping: ResultMapping, columnPrefix: String?,
            rsw: ResultSetWrapper?
    ): Boolean {
        val notNullColumns = resultMapping.notNullColumns
        if (notNullColumns != null && notNullColumns.isNotEmpty()) {
            val rs = rsw!!.resultSet
            for (column in notNullColumns) {
                rs.getObject(prependPrefix(column, columnPrefix))
                if (!rs.wasNull()) {
                    return true
                }
            }
            return false
        } else if (columnPrefix != null) {
            for (columnName in rsw!!.columnNames) {
                if (columnName.uppercase()
                                .startsWith(columnPrefix.uppercase())) {
                    return true
                }
            }
            return false
        }
        return true
    }

    @Throws(SQLException::class)
    private fun getNestedResultMap(rs: ResultSet, nestedResultMapId: String, columnPrefix: String?): ResultMap {
        val nestedResultMap = configuration.getResultMap(nestedResultMapId)
        return resolveDiscriminatedResultMap(rs, nestedResultMap, columnPrefix)
    }

    //
    // UNIQUE RESULT KEY
    //
    @Throws(SQLException::class)
    private fun createRowKey(resultMap: ResultMap, rsw: ResultSetWrapper?, columnPrefix: String?): CacheKey {
        val cacheKey = CacheKey()
        cacheKey.update(resultMap.id)
        val resultMappings = getResultMappingsForRowKey(resultMap)
        if (resultMappings.isEmpty()) {
            if (MutableMap::class.java.isAssignableFrom(resultMap.type)) {
                createRowKeyForMap(rsw, cacheKey)
            } else {
                createRowKeyForUnmappedProperties(resultMap, rsw, cacheKey, columnPrefix)
            }
        } else {
            createRowKeyForMappedProperties(resultMap, rsw, cacheKey, resultMappings, columnPrefix)
        }
        return if (cacheKey.updateCount < 2) {
            CacheKey.NULL_CACHE_KEY
        } else cacheKey
    }

    private fun combineKeys(rowKey: CacheKey, parentRowKey: CacheKey): CacheKey {
        if (rowKey.updateCount > 1 && parentRowKey.updateCount > 1) {
            val combinedKey: CacheKey = try {
                rowKey.clone()
            } catch (e: CloneNotSupportedException) {
                throw ExecutorException("Error cloning cache key.  Cause: $e", e)
            }
            combinedKey.update(parentRowKey)
            return combinedKey
        }
        return CacheKey.NULL_CACHE_KEY
    }

    private fun getResultMappingsForRowKey(resultMap: ResultMap): List<ResultMapping> {
        var resultMappings = resultMap.idResultMappings
        if (resultMappings.isEmpty()) {
            resultMappings = resultMap.propertyResultMappings
        }
        return resultMappings
    }

    @Throws(SQLException::class)
    private fun createRowKeyForMappedProperties(
            resultMap: ResultMap, rsw: ResultSetWrapper?,
            cacheKey: CacheKey, resultMappings: List<ResultMapping>, columnPrefix: String?
    ) {
        for (resultMapping in resultMappings) {
            if (resultMapping.isSimple) {
                val column = prependPrefix(resultMapping.column, columnPrefix)
                val th = resultMapping.typeHandler
                val mappedColumnNames = rsw!!.getMappedColumnNames(resultMap, columnPrefix)
                // Issue #114
                if (column != null && mappedColumnNames.contains(column.uppercase())) {
                    val value = th.getResult(rsw.resultSet, column)
                    if (value != null || configuration.isReturnInstanceForEmptyRow) {
                        cacheKey.update(column)
                        cacheKey.update(value)
                    }
                }
            }
        }
    }

    @Throws(SQLException::class)
    private fun createRowKeyForUnmappedProperties(
            resultMap: ResultMap, rsw: ResultSetWrapper?,
            cacheKey: CacheKey, columnPrefix: String?
    ) {
        val metaType = MetaClass.forClass(resultMap.type, reflectorFactory)
        val unmappedColumnNames = rsw!!.getUnmappedColumnNames(resultMap, columnPrefix)
        for (column in unmappedColumnNames) {
            var property = column
            if (!columnPrefix.isNullOrEmpty()) {
                // When columnPrefix is specified, ignore columns without the prefix.
                property = if (column.uppercase().startsWith(columnPrefix)) {
                    column.substring(columnPrefix.length)
                } else {
                    continue
                }
            }
            if (metaType.findProperty(property, configuration.isMapUnderscoreToCamelCase) != null) {
                val value = rsw.resultSet.getString(column)
                if (value != null) {
                    cacheKey.update(column)
                    cacheKey.update(value)
                }
            }
        }
    }

    @Throws(SQLException::class)
    private fun createRowKeyForMap(rsw: ResultSetWrapper?, cacheKey: CacheKey) {
        val columnNames = rsw!!.columnNames
        for (columnName in columnNames) {
            val value = rsw.resultSet.getString(columnName)
            if (value != null) {
                cacheKey.update(columnName)
                cacheKey.update(value)
            }
        }
    }

    private fun linkObjects(metaObject: MetaObject?, resultMapping: ResultMapping?, rowValue: Any) {
        val collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping,
                metaObject)
        if (collectionProperty != null) {
            val targetMetaObject = configuration.newMetaObject(collectionProperty)
            targetMetaObject.add(rowValue)
        } else {
            metaObject!!.setValue(resultMapping!!.property, rowValue)
        }
    }

    private fun instantiateCollectionPropertyIfAppropriate(
            resultMapping: ResultMapping?,
            metaObject: MetaObject?
    ): Any? {
        val propertyName = resultMapping!!.property
        var propertyValue = metaObject!!.getValue(propertyName)
        if (propertyValue == null) {
            var type = resultMapping.javaType
            if (type == null) {
                type = metaObject.getSetterType(propertyName)
            }
            try {
                if (objectFactory.isCollection(type)) {
                    propertyValue = objectFactory.create(type)
                    metaObject.setValue(propertyName, propertyValue)
                    return propertyValue
                }
            } catch (e: Exception) {
                throw ExecutorException(
                        "Error instantiating collection property for result '" + resultMapping.property
                                + "'.  Cause: " + e, e)
            }
        } else if (objectFactory.isCollection(propertyValue.javaClass)) {
            return propertyValue
        }
        return null
    }

    private fun hasTypeHandlerForResultObject(rsw: ResultSetWrapper?, resultType: Class<*>): Boolean {
        return if (rsw!!.columnNames.size == 1) {
            typeHandlerRegistry.hasTypeHandler(resultType,
                    rsw.getJdbcType(rsw.columnNames[0]))
        } else typeHandlerRegistry.hasTypeHandler(resultType)
    }

    companion object {
        private val DEFERRED = Any()
        fun findNestedResultMap(mappedStatement: MappedStatement?): String? {
            for (resultMap in mappedStatement!!.resultMaps) {
                if (resultMap.hasNestedResultMaps()) {
                    for (resultMapping in resultMap.resultMappings) {
                        val nestedResultMapId = resultMapping.nestedResultMapId
                        if (nestedResultMapId != null && nestedResultMapId.contains("_collection")) {
                            throw UnsupportedOperationException(
                                    "$nestedResultMapId collection resultmap not support page query")
                        }
                    }
                    return resultMap.id
                }
            }
            return null
        }

        fun findNestedResultMapType(mappedStatement: MappedStatement?): NestedResultMapType? {
            val resultMaps = mappedStatement!!.resultMaps
            for (resultMap in resultMaps) {
                if (resultMap.hasNestedResultMaps()) {
                    for (resultMapping in resultMap.resultMappings) {
                        val nestedResultMapId = resultMapping.nestedResultMapId
                        if (nestedResultMapId != null && nestedResultMapId.contains("_collection")) {
                            return NestedResultMapType(nestedResultMapId, true)
                        }
                    }
                    return NestedResultMapType(resultMap.id, false)
                }
            }
            return null
        }

        fun validateResultMaps(mappedStatement: MappedStatement?) {
            val resultMaps = mappedStatement!!.resultMaps
            if (resultMaps.size > 1) {
                throw ExecutorException("Multiples resultMaps  not supported")
            }
            val resultSets = mappedStatement.resultSets
            if (resultSets != null && resultSets.isNotEmpty()) {
                throw UnsupportedOperationException("mybatis resultSets not supported")
            }
        }
    }
}
