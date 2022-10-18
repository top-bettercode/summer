package top.bettercode.autodoc.gen

import org.atteo.evo.inflector.English
import top.bettercode.autodoc.core.Util.checkBlank
import top.bettercode.autodoc.core.Util.convert
import top.bettercode.autodoc.core.Util.parseList
import top.bettercode.autodoc.core.Util.toJsonString
import top.bettercode.autodoc.core.Util.toMap
import top.bettercode.autodoc.core.Util.type
import top.bettercode.autodoc.core.model.Field
import top.bettercode.autodoc.core.operation.DocOperation
import top.bettercode.autodoc.core.operation.DocOperationRequest
import top.bettercode.autodoc.core.operation.DocOperationResponse
import top.bettercode.generator.DataType
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.powerdesigner.PdmReader
import top.bettercode.generator.puml.PumlConverter
import top.bettercode.lang.decapitalized
import top.bettercode.lang.property.PropertiesSource
import top.bettercode.logging.operation.OperationRequestPart
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
        .map { Field(it.key, "Object", it.value) }
        .toSet()

    fun init(
        operation: DocOperation,
        extension: GeneratorExtension,
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
            extension.fixFields { fields, onlyDesc ->
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
            if (!(uriNeedFix.noneBlank() && reqHeadNeedFix.noneBlank() && paramNeedFix.noneBlank() && partNeedFix.noneBlank() && reqContentNeedFix.noneBlank() && resHeadNeedFix.noneBlank() && resContentNeedFix.noneBlank())) {
                uriNeedFix.fix(needFixFields = uriNeedFix, fuzzy = true)
                reqHeadNeedFix.fix(needFixFields = reqHeadNeedFix, fuzzy = true)
                paramNeedFix.fix(needFixFields = paramNeedFix, fuzzy = true)
                partNeedFix.fix(needFixFields = partNeedFix, fuzzy = true)
                reqContentNeedFix.fix(needFixFields = reqContentNeedFix, fuzzy = true)
                resHeadNeedFix.fix(needFixFields = resHeadNeedFix, fuzzy = true)
                resContentNeedFix.fix(needFixFields = resContentNeedFix, wrap = wrap, fuzzy = true)
            }
        }

        Autodoc.fields.forEach { name, desc ->
            request.uriVariablesExt.filter { it.name == name }.forEach { it.description = desc }
            request.headersExt.filter { it.name == name }.forEach { it.description = desc }
            request.parametersExt.filter { it.name == name }.forEach { it.description = desc }
            request.partsExt.filter { it.name == name }.forEach { it.description = desc }
            request.contentExt.filter { it.name == name }.forEach { it.description = desc }

            response.headersExt.filter { it.name == name }.forEach { it.description = desc }
            response.contentExt.filter { it.name == name }.forEach { it.description = desc }
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
        onlyDesc: Boolean = false,
        fuzzy: Boolean = false,
    ) {
        fixFieldTree(
            needFixFields,
            hasDesc = false,
            userDefault = false,
            wrap = wrap,
            onlyDesc = onlyDesc,
            fuzzy = fuzzy
        )
    }

    private fun GeneratorExtension.fixFields(
        fn: (Set<Field>, Boolean) -> Boolean
    ) {
        if (fn(messageFields, true)) return

        val ext = this

        val fixFields =
            { sources: Map<String, List<File>>, toTables: (file: File, module: String) -> List<Table> ->
                sources.forEach all@{ (module, files) ->
                    files.forEach { file ->
                        val tables = toTables(file, module)
                        for (tableName in Autodoc.tableNames) {
                            val table = tables.find { it.tableName == tableName }
                            if (table != null) {
                                if (fn(table.fields(), false)) {
                                    return@all
                                }
                            }
                        }
                        for (table in tables.filter { !Autodoc.tableNames.contains(it.tableName) }) {
                            if (fn(table.fields(), false)) {
                                return@all
                            }
                        }
                    }
                }
            }
        when (dataType) {
            DataType.DATABASE -> {
                datasources.forEach { (module, jdbc) ->
                    jdbc.schema = Autodoc.schema
                    jdbc.use {
                        Autodoc.tableNames.forEach { tableName ->
                            val table = table(tableName) {
                                it.ext = ext
                                it.module = module
                            }
                            if (table != null) {
                                if (fn(table.fields(), false)) {
                                    return
                                }
                            }
                        }
                        val tableNames = tableNames().toMutableList()
                        tableNames.removeAll(Autodoc.tableNames)
                        tableNames.forEach { tableName ->
                            val table = table(tableName) {
                                it.ext = ext
                                it.module = module
                            }
                            if (table != null) {
                                if (fn(table.fields(), false)) {
                                    return
                                }
                            }
                        }
                    }
                }
            }

            DataType.PUML -> {
                fixFields(pumlSources) { file, module ->
                    PumlConverter.toTables(file) {
                        it.ext = ext
                        it.module = module
                    }
                }
            }

            DataType.PDM -> {
                fixFields(pdmSources) { file, module ->
                    PdmReader.read(file) {
                        it.ext = ext
                        it.module = module
                    }
                }
            }
        }
    }

    private fun Table.fields(): Set<Field> {
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
        fields.add(Field(entityName, "Object", remarks))
        fields.add(Field(pathName, "Array", remarks))
        if (primaryKeys.size == 0) {
            fields.add(Field(entityName + "Entity", "Object", remarks))
        } else {
            if (primaryKeys.size > 1) {
                fields.add(Field(entityName + "Key", "String", remarks + "主键"))
                fields.add(
                    Field(
                        English.plural(entityName + "Key"),
                        "String",
                        remarks + "主键"
                    )
                )
            }
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
        onlyDesc: Boolean = false,
        fuzzy: Boolean = false
    ) {
        needFixFields.forEach { field ->
            val findField =
                fixField(
                    field = field,
                    hasDesc = hasDesc,
                    userDefault = userDefault,
                    wrap = wrap,
                    onlyDesc = onlyDesc,
                    fuzzy = fuzzy
                )
            fieldDescBundle.all().forEach { (k, v) ->
                field.description = field.description.replace(k, v)
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
        onlyDesc: Boolean = false,
        fuzzy: Boolean = false
    ): Field? {
        val findField = this.findPossibleField(
            name = field.name,
            type = field.value.type,
            hasDesc = hasDesc,
            fuzzy = fuzzy
        )
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


    fun Map<String, Any?>.toFields(
        fields: Set<Field>,
        expand: Boolean = false
    ): LinkedHashSet<Field> {
        return this.mapTo(LinkedHashSet()) { (k, v) ->
            val field = fields.field(k, v)
            if (expand) {
                val expandValue = field.value.toMap()
                if (expandValue != null) {
                    field.children = expandValue.toFields(field.children + fields, expand)
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


    private fun Set<Field>.field(name: String, value: Any?): Field {
        val type = (value?.type ?: "String")
        val field = findPossibleField(name = name, type = type) ?: Field(name = name, type = type)

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
        hasDesc: Boolean = false,
        fuzzy: Boolean = false
    ): Field? {
        return this.findField(name = name, type = type, hasDesc = hasDesc)
            ?: if (fuzzy) this.findFuzzyField(
                name = name,
                type = type,
                hasDesc = hasDesc
            ) else null
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
            name.startsWith("start") -> name.substringAfter("start")
                .decapitalized()

            name.endsWith("Start") -> name.substringBeforeLast("Start")
            name.startsWith("end") -> name.substringAfter("end")
                .decapitalized()

            name.endsWith("End") -> name.substringBeforeLast("End")
            name.endsWith("Pct") -> name.substringBeforeLast("Pct")
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
            ?: (set.find { it.name == name && it.type.substringBefore("(").equals(type, true) }
                ?.copy()
                ?: set.find { it.name.equals(name, true) && it.type.substringBefore("(") == type }
                    ?.copy())
            ?: set.find {
                it.name.equals(name, true) && it.type.substringBefore("(").equals(type, true)
            }?.copy())
            ?: set.find { it.name == name }?.copy()
            ?: set.find { it.name.equals(name, true) }?.copy()
        return field?.apply { this.name = name }
    }

}

