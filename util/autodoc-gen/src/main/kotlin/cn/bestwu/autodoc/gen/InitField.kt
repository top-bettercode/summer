package cn.bestwu.autodoc.gen

import cn.bestwu.autodoc.core.*
import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.autodoc.core.operation.DocOperation
import cn.bestwu.autodoc.core.operation.DocOperationRequest
import cn.bestwu.autodoc.core.operation.DocOperationResponse
import cn.bestwu.generator.DataType
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.database.entity.Table
import cn.bestwu.generator.powerdesigner.PdmReader
import cn.bestwu.generator.puml.PumlConverter
import cn.bestwu.logging.operation.OperationRequestPart
import org.atteo.evo.inflector.English
import java.io.File

/**
 *
 * @author Peter Wu
 */
object InitField {

    private val contentWrapFields: Set<String> = setOf("status", "message", "data", "trace", "errors")

    fun init(operation: DocOperation, extension: GeneratorExtension, allTables: Boolean, wrap: Boolean, defaultValueHeaders: Map<String, String>, defaultValueParams: Map<String, String>) {
        val request = operation.request as DocOperationRequest
        val response = operation.response as DocOperationResponse

        val uriNeedFix = request.uriVariablesExt.blankField()
        val reqHeadNeedFix = request.headersExt.blankField()
        val paramNeedFix = request.parametersExt.blankField()
        val partNeedFix = request.partsExt.blankField()
        val reqContentNeedFix = request.contentExt.blankField()
        val resHeadNeedFix = response.headersExt.blankField()
        val resContentNeedFix = response.contentExt.blankField()
        if (uriNeedFix.isNotEmpty() || reqHeadNeedFix.isNotEmpty() || paramNeedFix.isNotEmpty() || partNeedFix.isNotEmpty() || reqContentNeedFix.isNotEmpty() || resHeadNeedFix.isNotEmpty() || resContentNeedFix.isNotEmpty()) {
            extension.fixFields(allTables) { fields ->
                fields.fix(uriNeedFix)
                fields.fix(reqHeadNeedFix)
                fields.fix(paramNeedFix)
                fields.fix(partNeedFix)
                fields.fix(reqContentNeedFix)
                fields.fix(resHeadNeedFix)
                fields.fix(resContentNeedFix, wrap)

                uriNeedFix.noneBlank() && reqHeadNeedFix.noneBlank() && paramNeedFix.noneBlank() && partNeedFix.noneBlank() && reqContentNeedFix.noneBlank() && resHeadNeedFix.noneBlank() && resContentNeedFix.noneBlank()
            }
        }

        request.uriVariablesExt.checkBlank("request.uriVariablesExt")
        request.headersExt.checkBlank("request.headersExt").forEach {
            val defaultValueHeader = defaultValueHeaders[it.name]
            if (!defaultValueHeader.isNullOrBlank()) {
                it.defaultVal = defaultValueHeader
            }
        }
        request.parametersExt.checkBlank("request.parametersExt").forEach {
            val defaultValueParam = defaultValueParams[it.name]
            if (!defaultValueParam.isNullOrBlank()) {
                it.defaultVal = defaultValueParam
            }
        }
        request.partsExt.checkBlank("request.partsExt").forEach {
            val defaultValueParam = defaultValueParams[it.name]
            if (!defaultValueParam.isNullOrBlank()) {
                it.defaultVal = defaultValueParam
            }
        }
        request.contentExt.checkBlank("request.contentExt")
        response.headersExt.checkBlank("response.headersExt")
        response.contentExt.checkBlank("response.contentExt")
    }

    private fun Set<Field>.fix(needFixFields: Set<Field>, wrap: Boolean = false) {
        fixFieldTree(needFixFields, hasDesc = false, userDefault = false, wrap = wrap)
        needFixFields.fixFieldTree(needFixFields)
    }

    private fun GeneratorExtension.fixFields(allTables: Boolean, fn: (Set<Field>) -> Boolean) {
        val tableNames = linkedSetOf<String>()
        tableNames.addAll(Autodoc.tableNames)
        this.datasource.schema = Autodoc.schema

        when (this.dataType) {
            DataType.DATABASE -> {
                try {
                    if (allTables)
                        this.use {
                            tableNames.addAll(tableNames())
                        }
                    val ext = this
                    use {
                        for (tableName in tableNames) {
                            val table = table(tableName)
                            if (table != null) {
                                if (fn(table.fields(ext))) break
                            }
                        }
                    }
                } catch (ignore: ClassNotFoundException) {
                }
            }
            DataType.PUML -> {
                val tables = this.pumlAllSources.map { PumlConverter.toTables(it) }.flatten()
                this.fixFields(allTables, tableNames, tables, fn)
            }
            DataType.PDM -> {
                val tables = PdmReader.read(this.file(this.pdmSrc)).asSequence()
                this.fixFields(allTables, tableNames, tables, fn)
            }
        }
    }

    private fun GeneratorExtension.fixFields(allTables: Boolean, tableNames: LinkedHashSet<String>, tables: Sequence<Table>, fn: (Set<Field>) -> Boolean) {
        if (allTables)
            tableNames.addAll(tables.map { it.tableName })

        for (tableName in tableNames) {
            println("查询：$tableName 表数据结构")
            val table = tables.find { it.tableName == tableName }
                    ?: throw RuntimeException("未在(${tables.joinToString(",") { it.tableName }})中找到${tableName}表")
            if (fn(table.fields(this))) break
        }
    }

    private fun Table.fields(extension: GeneratorExtension): Set<Field> {
        val fields = columns.asSequence().mapTo(mutableSetOf()) { column ->
            var type = if (column.containsSize) "${column.javaType.shortNameWithoutTypeArguments}(${column.columnSize}${if (column.decimalDigits <= 0) "" else ",${column.decimalDigits}"})" else column.javaType.shortNameWithoutTypeArguments
            if (column.javaType.shortNameWithoutTypeArguments == "Date")//前端统一传毫秒数
                type = "Long"
            Field(column.javaName, type, column.remarks, column.columnDef
                    ?: "", "", required = column.nullable)
        }
        fields.addAll(fields.map { Field(English.plural(it.name), "Array", it.description) })
        fields.add(Field(entityName(extension), "Object", remarks))
        fields.add(Field(pathName(extension), "Array", remarks))
        return fields
    }

    fun extFieldExt(genProperties: GenProperties, operation: DocOperation) {
        operation.apply {
            request.apply {
                this as DocOperationRequest
                commonFields(genProperties, "request.uriVariables").fixFieldTree(uriVariablesExt)
                commonFields(genProperties, "request.headers").fixFieldTree(headersExt)
                commonFields(genProperties, "request.parameters").fixFieldTree(parametersExt)
                commonFields(genProperties, "request.parts").fixFieldTree(partsExt)
                commonFields(genProperties, "request.content").fixFieldTree(contentExt)
            }
            response.apply {
                this as DocOperationResponse
                commonFields(genProperties, "response.headers").fixFieldTree(headersExt)
                commonFields(genProperties, "response.content").fixFieldTree(contentExt)
            }
        }
    }


    private fun commonFields(genProperties: GenProperties, name: String): Set<Field> {
        genProperties.apply {
            val commonFields = commonFields(name, source)
            commonFields.addAll(commonFields(name, rootSource))
            return commonFields
        }
    }

    private fun commonFields(name: String, source: File?): LinkedHashSet<Field> {
        return if (source != null) {
            var file = File(source, "${name}.yml")
            if (!file.exists()) {
                file = File(source, "field.yml")
            }
            if (file.exists()) {
                file.parseList(Field::class.java)
            } else {
                linkedSetOf()
            }
        } else {
            linkedSetOf()
        }
    }

    private fun Set<Field>.fixFieldTree(needFixFields: Set<Field>, hasDesc: Boolean = true, userDefault: Boolean = true, wrap: Boolean = false) {
        needFixFields.forEach { field ->
            val findField = fixField(field = field, hasDesc = hasDesc, userDefault = userDefault, wrap = wrap)
            findField?.children?.fixFieldTree(field.children)
            fixFieldTree(field.children)
        }
    }

    private fun Set<Field>.fixField(field: Field, hasDesc: Boolean = false, coverType: Boolean = true, userDefault: Boolean = true, wrap: Boolean = false): Field? {
        val findField = this.findPossibleField(field.name, field.value.type, hasDesc)
        if (findField != null && (field.canCover || field.description.isBlank() || !findField.canCover) && (!wrap || !contentWrapFields.contains(field.name))) {
            field.canCover = findField.canCover
            if (userDefault)
                field.defaultVal = findField.defaultVal
            if (coverType || !findField.canCover)
                field.type = findField.type
            if (findField.description.isNotBlank())
                field.description = findField.description

            var tempVal = field.value
            if (tempVal.isBlank()) {
                tempVal = if (findField.value.isBlank()) field.defaultVal else findField.value
            }
            field.value = tempVal.convert(false)?.toJsonString(false) ?: ""
        }
        return findField
    }
}

fun Map<String, Any?>.toFields(fields: Set<Field>, expand: Boolean = false): LinkedHashSet<Field> {
    return this.mapTo(LinkedHashSet(), { (k, v) ->
        val field = fields.field(k, v)
        if (expand) {
            val expandValue = field.value.toMap()
            if (expandValue != null) {
                field.children = expandValue.toFields(field.children, expand)
            }
        }
        field
    })
}

fun Collection<OperationRequestPart>.toFields(fields: Set<Field>): LinkedHashSet<Field> {
    return this.mapTo(LinkedHashSet(), { fields.field(it.name, it.contentAsString).apply { partType = if (it.submittedFileName == null) "text" else "file" } })
}

private fun Set<Field>.blankField(): Set<Field> {
    return filter { it.description.isBlank() || it.canCover || it.children.anyblank() }.toSet()
}

private fun Set<Field>.field(name: String, value: Any?): Field {
    val type = (value?.type ?: "String")
    val field = findPossibleField(name, type) ?: Field(name = name, type = type)

    var tempVal = value
    if (tempVal == null || "" == tempVal) {
        tempVal = if (field.value.isBlank()) field.defaultVal else field.value
    }
    field.value = tempVal.convert(false)?.toJsonString(false) ?: ""
    return field
}


private fun Set<Field>.findPossibleField(name: String, type: String, hasDesc: Boolean = false): Field? {
    return this.findField(name, type, hasDesc) ?: this.findFuzzyField(name, type, hasDesc)
}

private fun Set<Field>.findFuzzyField(name: String, type: String, hasDesc: Boolean = false): Field? {
    val newName = when {
        name.endsWith("Name") -> name.substringBeforeLast("Name")
        name.endsWith("Url") -> name.substringBeforeLast("Url")
        name.endsWith("Urls") -> name.substringBeforeLast("Urls")
        name.endsWith("Path") -> name.substringBeforeLast("Path")
        else -> {
            return null
        }
    }
    val field = this.findField(newName, type, hasDesc)
    if (field != null) {
        field.name = name
        field.type = "String"
        field.defaultVal = ""
        field.description = field.description.split(Regex("[（(,:，：]"))[0]
    }
    return field
}

private fun Set<Field>.findField(name: String, type: String, hasDesc: Boolean = false): Field? {
    val set = (if (hasDesc) this.filter { it.description.isNotBlank() } else this)
    val field = (set.find { it.name == name && it.type.substringBefore("(") == type }?.copy()
            ?: (set.find { it.name == name && it.type.substringBefore("(").equals(type, true) }?.copy()
                    ?: set.find { it.name.equals(name, true) && it.type.substringBefore("(") == type }?.copy())
            ?: set.find { it.name.equals(name, true) && it.type.substringBefore("(").equals(type, true) }?.copy())
            ?: set.find { it.name == name }?.copy()
            ?: set.find { it.name.equals(name, true) }?.copy()
    return field?.apply { this.name = name }
}


