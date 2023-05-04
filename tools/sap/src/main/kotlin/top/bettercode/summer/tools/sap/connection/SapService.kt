package top.bettercode.summer.tools.sap.connection

import com.sap.conn.jco.*
import com.sap.conn.jco.ext.DestinationDataProvider
import com.sap.conn.jco.ext.Environment
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.beans.BeanWrapperImpl
import org.springframework.util.Assert
import org.springframework.util.ReflectionUtils
import org.springframework.util.StringUtils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.operation.PrettyPrintingContentModifier.modifyContent
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import top.bettercode.summer.tools.sap.annotation.SapField
import top.bettercode.summer.tools.sap.annotation.SapStructure
import top.bettercode.summer.tools.sap.annotation.SapTable
import top.bettercode.summer.tools.sap.config.SapProperties
import top.bettercode.summer.tools.sap.connection.SapService
import top.bettercode.summer.tools.sap.connection.pojo.ISapReturn
import java.beans.IntrospectionException
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

@LogMarker(SapService.LOG_MARKER_STRING)
class SapService(properties: SapProperties) {
    private val log = LoggerFactory.getLogger(SapService::class.java)
    private var filterNonAnnField = true
    private var filterNullFiled = false

    init {
        val destDataProvider = DestinationDataProviderImpl()
        destDataProvider.addDestinationProperties(ABAP_AS_POOLED, loadProperties(properties))
        Environment.registerDestinationDataProvider(destDataProvider)
    }

    private fun loadProperties(properties: SapProperties): Properties {
        val props = Properties()
        props.setProperty(DestinationDataProvider.JCO_USER, properties.user)
        props.setProperty(DestinationDataProvider.JCO_PASSWD, properties.passwd)
        props.setProperty(DestinationDataProvider.JCO_LANG, properties.lang)
        props.setProperty(DestinationDataProvider.JCO_CLIENT, properties.client)
        props.setProperty(DestinationDataProvider.JCO_SYSNR, properties.sysnr)
        props.setProperty(DestinationDataProvider.JCO_ASHOST, properties.ashost)
        props.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, properties.peakLimit)
        props.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, properties.poolCapacity)
        return props
    }

    @get:Throws(JCoException::class)
    private val destination: JCoDestination
        get() = JCoDestinationManager.getDestination(ABAP_AS_POOLED)

    @Throws(JCoException::class)
    fun getFunction(functionName: String?): JCoFunction {
        return destination.repository.getFunction(functionName)
    }

    fun setFilterNonAnnField(filterNonAnnField: Boolean): SapService {
        this.filterNonAnnField = filterNonAnnField
        return this
    }

    fun setFilterNullFiled(filterNullFiled: Boolean): SapService {
        this.filterNullFiled = filterNullFiled
        return this
    }

    operator fun <T : ISapReturn> invoke(functionName: String, data: Any?,
                                         returnClass: Class<T>): T {
        return invoke(functionName, data, returnClass, true)
    }

    operator fun <T : ISapReturn> invoke(functionName: String, data: Any?,
                                         returnClass: Class<T>,
                                         checkError: Boolean): T {
        var function: JCoFunction? = null
        val result: T
        var throwable: Throwable? = null
        var isSuccess = true
        val start = System.currentTimeMillis()
        var durationMillis: Long? = null
        return try {
            val destination = destination
            function = destination.repository.getFunction(functionName)
            Assert.notNull(function, "$functionName function not found")
            if (data != null) {
                parseInputParamObject(function, data)
            }
            function.execute(destination)
            val out: JCoRecord = if (returnClass.isAnnotationPresent(SapStructure::class.java)) {
                val struAnn = returnClass.getAnnotation(SapStructure::class.java)
                function.exportParameterList.getStructure(struAnn.value)
            } else {
                function.exportParameterList
            }
            result = toBean(function, out, returnClass)
            durationMillis = System.currentTimeMillis() - start
            if (!checkError || result.isOk) {
                isSuccess = result.isSuccess
                result
            } else {
                val msgText = result.message
                throw SapSysException(if (StringUtils.hasText(msgText)) msgText else "RFC请求失败")
            }
        } catch (e: Exception) {
            throwable = e
            when (e) {
                is SapException -> {
                    throw e
                }

                is SapSysException -> {
                    throw e
                }

                else -> {
                    var message = e.message
                    val msgRegex = "^Integer '(.*?)' has to many digits at field (.*?)$"
                    if (message!!.matches(msgRegex.toRegex())) {
                        val fieldValue = message.replace(msgRegex.toRegex(), "$1")
                        var fieldName = message.replace(msgRegex.toRegex(), "$2")
                        val jCoField = getField(function!!.importParameterList,
                                fieldName)
                        if (jCoField != null) {
                            fieldName = jCoField.description
                            val length = jCoField.length
                            message = String.format("%s的长度为%d，\"%s\"超出长度限制", fieldName, length,
                                    fieldValue)
                        } else {
                            message = String.format("%s超出长度限制", fieldValue)
                        }
                    }
                    throw SapException(message, e)
                }
            }
        } finally {
            if (durationMillis == null) {
                durationMillis = System.currentTimeMillis() - start
            }
            var exception = printException(function)
            if (!StringUtils.hasText(exception) && throwable != null) {
                exception = valueOf(throwable, true)
            }
            if (throwable != null || !isSuccess) {
                if (log.isWarnEnabled) {
                    log.warn(LOG_MARKER, "\nDURATION MILLIS : {}\n{}\n{}", durationMillis,
                            printFunctionList(function),
                            exception)
                }
            } else if (log.isInfoEnabled) {
                log.info(LOG_MARKER, "\nDURATION MILLIS : {}\n{}\n{}", durationMillis,
                        printFunctionList(function),
                        exception)
            }
        }
    }

    private fun getField(jCoFields: Iterable<JCoField>, fieldName: String): JCoField? {
        for (jCoField in jCoFields) {
            if (jCoField.isTable) {
                val field = getField(jCoField.table, fieldName)
                if (field != null) {
                    return field
                }
            } else if (jCoField.isStructure) {
                val field = getField(jCoField.structure, fieldName)
                if (field != null) {
                    return field
                }
            } else {
                val name = jCoField.name
                if (name == fieldName) {
                    return jCoField
                }
            }
        }
        return null
    }

    private fun printFunctionList(function: JCoFunction?): String {
        if (function == null) {
            return ""
        }
        val xml = function.toXML()
        return modifyContent(xml)
    }

    private fun printException(function: JCoFunction?): String {
        if (function == null) {
            return ""
        }
        val exceptionList = function.exceptionList
        val sb = StringBuilder()
        if (exceptionList != null) {
            for (exception in exceptionList) {
                sb.append(valueOf(exception, true)).append("\n")
            }
        }
        return sb.toString()
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalAccessException::class, IllegalArgumentException::class)
    private fun parseInputParamObject(function: JCoFunction?, data: Any) {
        val input = function!!.importParameterList
        val fields = data.javaClass.declaredFields
        var fieldName: String
        val beanWrapper = BeanWrapperImpl(data)
        for (field in fields) {
            val fv = beanWrapper.getPropertyValue(field.name)
            fieldName = field.name
            if (fv == null && filterNullFiled) {
                log.info(LOG_MARKER, "Not Setting SAP param: $fieldName is null")
            } else if (field.isAnnotationPresent(SapStructure::class.java)) {
                val struAnn = field.getAnnotation(SapStructure::class.java)
                val struJco = input.getStructure(struAnn.value)
                val map = toSapParamMap(fv)
                if (!map.isNullOrEmpty()) {
                    for ((key, value) in map) {
                        struJco.setValue(key, value)
                    }
                }
            } else if (field.isAnnotationPresent(SapTable::class.java)) {
                val sapTable = field.getAnnotation(SapTable::class.java)
                val tableParameterList = function.tableParameterList
                Assert.notNull(tableParameterList,
                        function.name + " function Table Parameter List not found")
                val table = tableParameterList.getTable(sapTable.value)
                if (fv is List<*>) {
                    val objList = fv as List<Any>
                    for (obj in objList) {
                        table.appendRow()
                        val map: Map<String, Any?>? = if (obj is Map<*, *>) {
                            obj as Map<String, Any?>
                        } else {
                            toSapParamMap(obj)
                        }
                        if (!map.isNullOrEmpty()) {
                            for ((key, value) in map) {
                                table.setValue(key, value)
                            }
                        }
                    }
                }
            } else if (field.isAnnotationPresent(SapField::class.java)) {
                val sapField = field.getAnnotation(SapField::class.java)
                if (sapField?.value != null) {
                    fieldName = sapField.value
                }
                input.setValue(fieldName, fv)
            } else if (!filterNonAnnField) {
                input.setValue(fieldName, fv)
            }
        }
    }

    @Throws(InstantiationException::class, IllegalAccessException::class, IntrospectionException::class, IllegalArgumentException::class, InvocationTargetException::class, NoSuchMethodException::class)
    private fun <T : Any> toBean(function: JCoFunction?, out: JCoRecord, returnClass: Class<T>): T {
        val result = returnClass.getDeclaredConstructor().newInstance()
        val beanWrapper = BeanWrapperImpl(result)
        ReflectionUtils.doWithFields(returnClass) { field: Field ->
            try {
                var fieldName = field.name
                val propertyType = Objects.requireNonNull(
                        beanWrapper.getPropertyTypeDescriptor(fieldName)).type
                if (field.isAnnotationPresent(SapStructure::class.java)) {
                    val struAnn = field.getAnnotation(SapStructure::class.java)
                    val fieldObj = toBean(function, out.getStructure(struAnn.value), propertyType)
                    beanWrapper.setPropertyValue(fieldName, fieldObj)
                } else if (field.isAnnotationPresent(SapTable::class.java)) {
                    val tabAnn = field.getAnnotation(SapTable::class.java)
                    val tb = function!!.tableParameterList.getTable(tabAnn.value)
                    beanWrapper.setPropertyValue(fieldName, this.toList<Any>(tb, field.genericType))
                } else if (field.isAnnotationPresent(SapField::class.java)) {
                    val sapField = field.getAnnotation(SapField::class.java)
                    if (sapField?.value != null) {
                        fieldName = sapField.value
                        beanWrapper.setPropertyValue(field.name, out.getValue(fieldName))
                    }
                } else if (!filterNonAnnField) {
                    beanWrapper.setPropertyValue(fieldName, out.getValue(fieldName))
                }
            } catch (e: IntrospectionException) {
                throw SapException(e)
            } catch (e: InstantiationException) {
                throw SapException(e)
            } catch (e: InvocationTargetException) {
                throw SapException(e)
            } catch (e: NoSuchMethodException) {
                throw SapException(e)
            }
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(InstantiationException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class, IntrospectionException::class, NoSuchMethodException::class)
    private fun <T : Any> toList(table: JCoTable, classType: Type): List<T?> {
        val tableList: MutableList<T?> = ArrayList()
        val params = (classType as ParameterizedType).actualTypeArguments
        val elementClass = params[0] as Class<*>
        var element: T? = null
        for (i in 0 until table.numRows) {
            table.row = i
            if (element is Map<*, *>) {
                val row: MutableMap<String, Any> = HashMap()
                for (j in 0 until table.numRows) {
                    for (field in table) {
                        row[field.name] = field.value
                    }
                }
                element = row as T
            } else {
                element = elementClass.getDeclaredConstructor().newInstance() as T
                val fields: Array<Field> = element.javaClass.declaredFields
                val beanWrapper = BeanWrapperImpl(element)
                for (fld in table) {
                    for (field in fields) {
                        var fieldName = field.name
                        if (field.isAnnotationPresent(SapField::class.java)) {
                            fieldName = field.getAnnotation(SapField::class.java).value
                        }
                        if (fieldName == fld.name) {
                            beanWrapper.setPropertyValue(field.name, fld.value)
                            break
                        }
                    }
                }
            }
            tableList.add(element)
        }
        return tableList
    }

    @Throws(IllegalAccessException::class, IllegalArgumentException::class)
    private fun toSapParamMap(obj: Any?): Map<String, Any?>? {
        if (obj == null) {
            return null
        }
        val sapObjMap: MutableMap<String, Any?> = HashMap()
        val fields = obj.javaClass.declaredFields
        val beanWrapper = BeanWrapperImpl(obj)
        for (field in fields) {
            var fv: Any?
            try {
                fv = beanWrapper.getPropertyValue(field.name)
            } catch (var12: Exception) {
                field.isAccessible = true
                fv = field[obj]
            }
            if (fv != null || !filterNullFiled) {
                var fieldName = field.name
                if (field.isAnnotationPresent(SapField::class.java)) {
                    val sapField = field.getAnnotation(SapField::class.java)
                    if (sapField?.value != null) {
                        fieldName = sapField.value
                        sapObjMap[fieldName] = fv
                    }
                }
                if (!filterNonAnnField) {
                    sapObjMap[fieldName] = fv
                }
            }
        }
        return sapObjMap
    }

    companion object {
        const val LOG_MARKER_STRING = "sap"
        private val LOG_MARKER = MarkerFactory.getMarker(LOG_MARKER_STRING)
        private const val ABAP_AS_POOLED = "ABAP_AS_WITH_POOL"
    }
}
