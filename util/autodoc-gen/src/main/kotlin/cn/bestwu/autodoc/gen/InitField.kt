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
import cn.bestwu.lang.property.PropertiesSource
import cn.bestwu.logging.operation.OperationRequestPart
import org.atteo.evo.inflector.English
import java.io.File

/**
 *
 * @author Peter Wu
 */
object InitField {

    private val contentWrapFields: Set<String> =
        setOf("status", "message", "data", "trace", "errors")
    private val fieldDescBundle: PropertiesSource = PropertiesSource.of("field-desc-replace")
    private val messageFields = PropertiesSource.of("messages").all()
        .map { Field(it.key.toString(), "Object", it.value.toString()) }
        .toSet()

    fun init(
        operation: DocOperation,
        extension: GeneratorExtension,
        allTables: Boolean,
        wrap: Boolean,
        defaultValueHeaders: Map<String, String>,
        defaultValueParams: Map<String, String>
    ) {
        val request = operation.request as DocOperationRequest
        val response = operation.response as DocOperationResponse

        var uriNeedFix = request.uriVariablesExt.blankField()
        var reqHeadNeedFix = request.headersExt.blankField()
        var paramNeedFix = request.parametersExt.blankField()
        var partNeedFix = request.partsExt.blankField()
        var reqContentNeedFix = request.contentExt.blankField()
        var resHeadNeedFix = response.headersExt.blankField()
        var resContentNeedFix = response.contentExt.blankField()
        if (uriNeedFix.isNotEmpty() || reqHeadNeedFix.isNotEmpty() || paramNeedFix.isNotEmpty() || partNeedFix.isNotEmpty() || reqContentNeedFix.isNotEmpty() || resHeadNeedFix.isNotEmpty() || resContentNeedFix.isNotEmpty()) {
            extension.fixFields(allTables) { fields, onlyDesc ->
                fields.fix(needFixFields = uriNeedFix, onlyDesc = onlyDesc)
                fields.fix(needFixFields = reqHeadNeedFix, onlyDesc = onlyDesc)
                fields.fix(needFixFields = paramNeedFix, onlyDesc = onlyDesc)
                fields.fix(needFixFields = partNeedFix, onlyDesc = onlyDesc)
                fields.fix(needFixFields = reqContentNeedFix, onlyDesc = onlyDesc)
                fields.fix(needFixFields = resHeadNeedFix, onlyDesc = onlyDesc)
                fields.fix(needFixFields = resContentNeedFix, wrap = wrap, onlyDesc = onlyDesc)

                uriNeedFix = request.uriVariablesExt.blankField(false)
                reqHeadNeedFix = request.headersExt.blankField(false)
                paramNeedFix = request.parametersExt.blankField(false)
                partNeedFix = request.partsExt.blankField(false)
                reqContentNeedFix = request.contentExt.blankField(false)
                resHeadNeedFix = response.headersExt.blankField(false)
                resContentNeedFix = response.contentExt.blankField(false)

                uriNeedFix.noneBlank() && reqHeadNeedFix.noneBlank() && paramNeedFix.noneBlank() && partNeedFix.noneBlank() && reqContentNeedFix.noneBlank() && resHeadNeedFix.noneBlank() && resContentNeedFix.noneBlank()
            }
            if (uriNeedFix.noneBlank() && reqHeadNeedFix.noneBlank() && paramNeedFix.noneBlank() && partNeedFix.noneBlank() && reqContentNeedFix.noneBlank() && resHeadNeedFix.noneBlank() && resContentNeedFix.noneBlank()) {
                uriNeedFix.fix(uriNeedFix)
                reqHeadNeedFix.fix(reqHeadNeedFix)
                paramNeedFix.fix(paramNeedFix)
                partNeedFix.fix(partNeedFix)
                reqContentNeedFix.fix(reqContentNeedFix)
                resHeadNeedFix.fix(resHeadNeedFix)
                resContentNeedFix.fix(resContentNeedFix, wrap)
            }
        }

        request.uriVariablesExt.checkBlank("request.uriVariablesExt")
        request.headersExt.checkBlank("request.headersExt")
        request.parametersExt.checkBlank("request.parametersExt")
        request.partsExt.checkBlank("request.partsExt")
        request.contentExt.checkBlank("request.contentExt")

        response.headersExt.checkBlank("response.headersExt")
        response.contentExt.checkBlank("response.contentExt")

        if (defaultValueHeaders.isNotEmpty()) {
            request.headersExt.forEach {
                val defaultValueHeader = defaultValueHeaders[it.name]
                if (!defaultValueHeader.isNullOrBlank()) {
                    it.defaultVal = defaultValueHeader
                }
            }
        }
        if (defaultValueParams.isNotEmpty()) {
            request.parametersExt.forEach {
                val defaultValueParam = defaultValueParams[it.name]
                if (!defaultValueParam.isNullOrBlank()) {
                    it.defaultVal = defaultValueParam
                }
            }
            request.partsExt.forEach {
                val defaultValueParam = defaultValueParams[it.name]
                if (!defaultValueParam.isNullOrBlank()) {
                    it.defaultVal = defaultValueParam
                }
            }
        }
    }

    private fun Set<Field>.fix(
        needFixFields: Set<Field>,
        wrap: Boolean = false,
        onlyDesc: Boolean = false
    ) {
        fixFieldTree(
            needFixFields,
            hasDesc = false,
            userDefault = false,
            wrap = wrap,
            onlyDesc = onlyDesc
        )
    }

    private fun GeneratorExtension.fixFields(
        allTables: Boolean,
        fn: (Set<Field>, Boolean) -> Boolean
    ) {
        this.datasource.schema = Autodoc.schema

        when (this.dataType) {
            DataType.DATABASE -> {
                try {
                    val ext = this
                    use {
                        for (tableName in Autodoc.tableNames) {
                            val table = table(tableName)
                            if (table != null) {
                                if (fn(table.fields(extension = ext), false)) break
                            }
                        }
                        fn(messageFields, true)
                        if (allTables) {
                            val tableNames =
                                tableNames().filter { !Autodoc.tableNames.contains(it) }
                            for (tableName in tableNames) {
                                val table = table(tableName)
                                if (table != null) {
                                    if (fn(table.fields(extension = ext), false)) break
                                }
                            }
                        }
                    }
                } catch (ignore: ClassNotFoundException) {
                }
            }
            DataType.PUML -> {
                val tables = this.pumlAllSources.map { PumlConverter.toTables(it) }.flatten()
                this.fixFields(allTables, tables, fn)
            }
            DataType.PDM -> {
                val tables = PdmReader.read(this.file(this.pdmSrc)).asSequence()
                this.fixFields(allTables, tables, fn)
            }
        }
    }

    private fun GeneratorExtension.fixFields(
        allTables: Boolean,
        tables: Sequence<Table>,
        fn: (Set<Field>, Boolean) -> Boolean
    ) {
        for (tableName in Autodoc.tableNames) {
            val table = tables.find { it.tableName == tableName }
                ?: throw RuntimeException("未在(${tables.joinToString(",") { it.tableName }})中找到${tableName}表")

            if (fn(table.fields(this), false)) break
        }

        fn(messageFields, true)

        if (allTables) {
            val needTables = tables.filter { !Autodoc.tableNames.contains(it.tableName) }
            for (table in needTables) {
                if (fn(table.fields(this), false)) break
            }
        }
    }

    private fun Table.fields(extension: GeneratorExtension): Set<Field> {
        val fields = columns.flatMapTo(mutableSetOf()) { column ->
            var type =
                if (column.containsSize) "${column.javaType.shortNameWithoutTypeArguments}(${column.columnSize}${if (column.decimalDigits <= 0) "" else ",${column.decimalDigits}"})" else column.javaType.shortNameWithoutTypeArguments
            if (column.javaType.shortNameWithoutTypeArguments == "Date")//前端统一传毫秒数
                type = "Long"
            setOf(
                Field(
                    column.javaName, type, column.remarks, column.columnDef
                        ?: "", "", required = column.nullable
                ), Field(
                    column.columnName, type, column.remarks, column.columnDef
                        ?: "", "", required = column.nullable
                )
            )
        }
        fields.addAll(fields.map { Field(English.plural(it.name), "Array", it.description) })
        fields.add(Field(entityName(extension), "Object", remarks))
        fields.add(Field(pathName(extension), "Array", remarks))
        if (primaryKeys.size > 1) {
            fields.add(Field(entityName(extension) + "Key", "String", remarks + "主键"))
        }
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

    private fun Set<Field>.fixFieldTree(
        needFixFields: Set<Field>,
        hasDesc: Boolean = true,
        userDefault: Boolean = true,
        wrap: Boolean = false,
        onlyDesc: Boolean = false
    ) {
        needFixFields.forEach { field ->
            val findField =
                fixField(
                    field = field,
                    hasDesc = hasDesc,
                    userDefault = userDefault,
                    wrap = wrap,
                    onlyDesc = onlyDesc
                )
            fieldDescBundle.all().forEach { (k, v) ->
                field.description = field.description.replace(k as String, v as String)
            }

            findField?.children?.fixFieldTree(field.children)
            fixFieldTree(field.children)
        }
    }

    private fun Set<Field>.fixField(
        field: Field,
        hasDesc: Boolean = false,
        coverType: Boolean = true,
        userDefault: Boolean = true,
        wrap: Boolean = false,
        onlyDesc: Boolean = false
    ): Field? {
        val findField = this.findPossibleField(field.name, field.value.type, hasDesc)
        if (findField != null && (field.canCover || field.description.isBlank() || !findField.canCover) && (!wrap || !contentWrapFields.contains(
                field.name
            ))
        ) {
            if (onlyDesc) {
                if (findField.description.isNotBlank())
                    field.description = findField.description
            } else {
                field.canCover = findField.canCover
                if (userDefault)
                    field.defaultVal = findField.defaultVal
                if (coverType || !findField.canCover)
                    field.type = findField.type
                if (findField.description.isNotBlank())
                    field.description = findField.description

                var tempVal = field.value
                if (tempVal.isBlank()) {
                    tempVal = field.defaultVal
                }
                field.value = tempVal.convert(false)?.toJsonString(false) ?: ""
            }
        }
        return findField
    }
}

fun Map<String, Any?>.toFields(fields: Set<Field>, expand: Boolean = false): LinkedHashSet<Field> {
    return this.mapTo(LinkedHashSet()) { (k, v) ->
        val field = fields.field(k, v)
        if (expand) {
            val expandValue = field.value.toMap()
            if (expandValue != null) {
                field.children = expandValue.toFields(field.children, expand)
            } else {
                field.children = LinkedHashSet()
            }
        }
        field
    }
}

fun Collection<OperationRequestPart>.toFields(fields: Set<Field>): LinkedHashSet<Field> {
    return this.mapTo(
        LinkedHashSet()
    ) {
        fields.field(it.name, it.contentAsString)
            .apply { partType = if (it.submittedFileName == null) "text" else "file" }
    }
}

private fun Set<Field>.blankField(canConver: Boolean = true): Set<Field> {
    return filter {
        it.description.isBlank() || canConver && it.canCover || it.children.anyblank(
            canConver
        )
    }.toSet()
}

private fun Set<Field>.noneBlank(): Boolean {
    return all { it.description.isNotBlank() && it.children.noneBlank() }
}

private fun Set<Field>.anyblank(canConver: Boolean): Boolean {
    return any {
        it.description.isBlank() || canConver && it.canCover || it.children.anyblank(
            canConver
        )
    }
}

private fun Set<Field>.field(name: String, value: Any?): Field {
    val type = (value?.type ?: "String")
    val field = findPossibleField(name, type) ?: Field(name = name, type = type)

    var tempVal = value
    if (tempVal == null || "" == tempVal) {
        tempVal = field.defaultVal
    }

    field.value = tempVal.convert(false)?.toJsonString(false) ?: ""

    return field
}


private fun Set<Field>.findPossibleField(
    name: String,
    type: String,
    hasDesc: Boolean = false
): Field? {
    return this.findField(name, type, hasDesc) ?: this.findFuzzyField(name, type, hasDesc)
}

private fun Set<Field>.findFuzzyField(
    name: String,
    type: String,
    hasDesc: Boolean = false
): Field? {
    val newName = when {
        name.endsWith("Name") -> name.substringBeforeLast("Name")
        name.endsWith("Url") -> name.substringBeforeLast("Url")
        name.endsWith("Urls") -> name.substringBeforeLast("Urls")
        name.endsWith("Path") -> name.substringBeforeLast("Path")
        name.startsWith("start") -> name.substringAfter("start").decapitalize()
        name.endsWith("Start") -> name.substringBeforeLast("Start")
        name.startsWith("end") -> name.substringAfter("end").decapitalize()
        name.endsWith("End") -> name.substringBeforeLast("End")
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
        if (name.startsWith("start") || name.endsWith("Start"))
            field.description = "开始" + field.description
        if (name.startsWith("end") || name.endsWith("End"))
            field.description = "结束" + field.description
    }
    return field
}

private fun Set<Field>.findField(name: String, type: String, hasDesc: Boolean = false): Field? {
    val set = (if (hasDesc) this.filter { it.description.isNotBlank() } else this)
    val field = (set.find { it.name == name && it.type.substringBefore("(") == type }?.copy()
        ?: (set.find { it.name == name && it.type.substringBefore("(").equals(type, true) }?.copy()
            ?: set.find { it.name.equals(name, true) && it.type.substringBefore("(") == type }
                ?.copy())
        ?: set.find {
            it.name.equals(name, true) && it.type.substringBefore("(").equals(type, true)
        }?.copy())
        ?: set.find { it.name == name }?.copy()
        ?: set.find { it.name.equals(name, true) }?.copy()
    return field?.apply { this.name = name }
}


