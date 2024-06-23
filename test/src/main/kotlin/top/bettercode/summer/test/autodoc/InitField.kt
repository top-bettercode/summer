package top.bettercode.summer.test.autodoc

import org.atteo.evo.inflector.English
import top.bettercode.summer.tools.autodoc.AutodocUtil.checkBlank
import top.bettercode.summer.tools.autodoc.AutodocUtil.convert
import top.bettercode.summer.tools.autodoc.AutodocUtil.parseList
import top.bettercode.summer.tools.autodoc.AutodocUtil.toJsonString
import top.bettercode.summer.tools.autodoc.AutodocUtil.toMap
import top.bettercode.summer.tools.autodoc.AutodocUtil.type
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import top.bettercode.summer.tools.autodoc.operation.DocOperationRequest
import top.bettercode.summer.tools.autodoc.operation.DocOperationResponse
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.puml.PumlConverter
import top.bettercode.summer.tools.lang.decapitalized
import top.bettercode.summer.tools.lang.operation.Operation
import top.bettercode.summer.tools.lang.operation.OperationRequestPart
import top.bettercode.summer.tools.lang.property.PropertiesSource
import java.io.File

/**
 *
 * @author Peter Wu
 */
object InitField {

    private val contentWrapFields: Set<String> =
        setOf("status", "message", "data", "trace", "errors")
    private val fieldDescBundle: PropertiesSource = PropertiesSource.of("field-desc-replace")
    private val messageFields =
        setOf(Field(name = "lines", description = "行信息")) + PropertiesSource.of("messages").all()
            .map { Field(name = it.key, description = it.value) }
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
            extension.fixFields { fields, onlyDesc, fixNoDescField ->
                fields.fixFieldTree(
                    needFixFields = uriNeedFix,
                    fixNoDescField = fixNoDescField,
                    userDefault = false,
                    onlyDesc = onlyDesc
                )
                fields.fixFieldTree(
                    needFixFields = reqHeadNeedFix,
                    fixNoDescField = fixNoDescField,
                    userDefault = false,
                    onlyDesc = onlyDesc
                )
                fields.fixFieldTree(
                    needFixFields = paramNeedFix,
                    fixNoDescField = fixNoDescField,
                    userDefault = false,
                    onlyDesc = onlyDesc
                )
                fields.fixFieldTree(
                    needFixFields = partNeedFix,
                    fixNoDescField = fixNoDescField,
                    userDefault = false,
                    onlyDesc = onlyDesc
                )
                fields.fixFieldTree(
                    needFixFields = reqContentNeedFix,
                    fixNoDescField = fixNoDescField,
                    userDefault = false,
                    onlyDesc = onlyDesc
                )
                fields.fixFieldTree(
                    needFixFields = resHeadNeedFix,
                    fixNoDescField = fixNoDescField,
                    userDefault = false,
                    onlyDesc = onlyDesc
                )
                fields.fixFieldTree(
                    needFixFields = resContentNeedFix,
                    wrap = wrap,
                    fixNoDescField = fixNoDescField,
                    userDefault = false,
                    onlyDesc = onlyDesc
                )

                uriNeedFix = request.uriVariablesExt.blankField(canConver = false)
                reqHeadNeedFix = request.headersExt.blankField(canConver = false)
                paramNeedFix = request.parametersExt.blankField(canConver = false)
                partNeedFix = request.partsExt.blankField(canConver = false)
                reqContentNeedFix = request.contentExt.blankField(canConver = false)
                resHeadNeedFix = response.headersExt.blankField(canConver = false)
                resContentNeedFix = response.contentExt.blankField(canConver = false)

                uriNeedFix.noneBlank() && reqHeadNeedFix.noneBlank() && paramNeedFix.noneBlank() && partNeedFix.noneBlank() && reqContentNeedFix.noneBlank() && resHeadNeedFix.noneBlank() && resContentNeedFix.noneBlank()
            }
            if (!(uriNeedFix.noneBlank() && reqHeadNeedFix.noneBlank() && paramNeedFix.noneBlank() && partNeedFix.noneBlank() && reqContentNeedFix.noneBlank() && resHeadNeedFix.noneBlank() && resContentNeedFix.noneBlank())) {
                request.uriVariablesExt.fixFieldTree(
                    needFixFields = uriNeedFix,
                    fixNoDescField = true,
                    hasDesc = true,
                    userDefault = false,
                    fuzzy = true
                )
                request.headersExt.fixFieldTree(
                    needFixFields = reqHeadNeedFix,
                    fixNoDescField = true,
                    hasDesc = true,
                    userDefault = false,
                    fuzzy = true
                )
                request.parametersExt.fixFieldTree(
                    needFixFields = paramNeedFix,
                    fixNoDescField = true,
                    hasDesc = true,
                    userDefault = false,
                    fuzzy = true
                )
                request.partsExt.fixFieldTree(
                    needFixFields = partNeedFix,
                    fixNoDescField = true,
                    hasDesc = true,
                    userDefault = false,
                    fuzzy = true
                )
                request.contentExt.fixFieldTree(
                    needFixFields = reqContentNeedFix,
                    fixNoDescField = true,
                    hasDesc = true,
                    userDefault = false,
                    fuzzy = true
                )
                response.headersExt.fixFieldTree(
                    needFixFields = resHeadNeedFix,
                    fixNoDescField = true,
                    hasDesc = true,
                    userDefault = false,
                    fuzzy = true
                )
                response.contentExt.fixFieldTree(
                    needFixFields = resContentNeedFix,
                    fixNoDescField = true,
                    hasDesc = true,
                    wrap = wrap,
                    userDefault = false,
                    fuzzy = true
                )
            }
        }

        Autodoc.fields.forEach { (name, desc) ->
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

    private fun GeneratorExtension.fixFields(
        fn: (Set<Field>, Boolean, Boolean) -> Boolean
    ) {
        if (fn(messageFields, true, false)) return

        val fixFields =
            { sources: Map<String, List<File>>, toTables: (file: File, module: String) -> List<Table> ->
                sources.forEach all@{ (module, files) ->
                    files.forEach { file ->
                        val tables = toTables(file, module)
                        for (tableName in Autodoc.tableNames) {
                            val table = tables.find { it.tableName == tableName }
                            if (table != null) {
                                if (fn(
                                        table.fields(),
                                        false,
                                        !GeneratorExtension.isDefaultModule(module)
                                    )
                                ) {
                                    return@all
                                }
                            }
                        }
                        for (table in tables.filter { !Autodoc.tableNames.contains(it.tableName) }) {
                            if (fn(
                                    table.fields(),
                                    false,
                                    !GeneratorExtension.isDefaultModule(module)
                                )
                            ) {
                                return@all
                            }
                        }
                    }
                }
            }

        fixFields(pumlSources) { file, module ->
            PumlConverter.toTables(this.database(module), file)
        }
    }

    private fun Table.fields(): Set<Field> {
        val fields = columns.flatMapTo(mutableSetOf()) { column ->
            var type =
                if (column.containsSize) "${column.javaType.shortNameWithoutTypeArguments}(${column.columnSize}${if (column.decimalDigits > 0) ",${column.decimalDigits}" else ""})" else column.javaType.shortNameWithoutTypeArguments
            if (column.javaType.shortNameWithoutTypeArguments in arrayOf(
                    "Date",
                    "LocalDate",
                    "LocalDateTime"
                )
            )//前端统一传毫秒数
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
        fields.addAll(fields.map {
            Field(
                name = English.plural(it.name),
                description = it.description
            )
        })
        fields.add(Field(name = entityName, description = remarks))
        fields.add(Field(name = pathName, description = remarks))
        if (primaryKeys.size == 0) {
            fields.add(Field(name = entityName + "Entity", description = remarks))
        } else {
            if (primaryKeys.size > 1) {
                fields.add(Field(name = entityName + "Key", description = remarks + "主键"))
                fields.add(
                    Field(
                        name = English.plural(entityName + "Key"),
                        description = remarks + "主键"
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
        fixNoDescField: Boolean = false,
        hasDesc: Boolean = true,
        userDefault: Boolean = true,
        wrap: Boolean = false,
        onlyDesc: Boolean = false,
        fuzzy: Boolean = false
    ) {
        needFixFields.forEach { field ->
            val findField = if (field.description.isBlank() || !fixNoDescField) {
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
                        if (findField.type.isNotBlank())
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
                findField
            } else null

            fieldDescBundle.all().forEach { (k, v) ->
                field.description = field.description.replace(k, v)
            }

            val children = findField?.children
            if (!children.isNullOrEmpty()) {
                children.fixFieldTree(
                    needFixFields = field.children,
                    fixNoDescField = fixNoDescField,
                    hasDesc = hasDesc,
                    userDefault = userDefault,
                    wrap = wrap,
                    onlyDesc = onlyDesc,
                    fuzzy = fuzzy
                )
            } else if (fuzzy) {
                field.children.fixFieldTree(
                    needFixFields = field.children,
                    fixNoDescField = fixNoDescField,
                    hasDesc = hasDesc,
                    userDefault = userDefault,
                    wrap = wrap,
                    onlyDesc = onlyDesc,
                    fuzzy = true
                )
            }

            fixFieldTree(
                needFixFields = field.children,
                fixNoDescField = fixNoDescField,
                hasDesc = hasDesc,
                userDefault = userDefault,
                wrap = wrap,
                onlyDesc = onlyDesc,
                fuzzy = fuzzy
            )
        }
    }

    private fun Set<Field>.blankField(canConver: Boolean = true): Set<Field> {
        return filter {
            it.isBlank(canConver) || it.children.anyBlank(canConver)
        }.toSet()
    }

    private fun Set<Field>.noneBlank(): Boolean {
        return all { it.description.isNotBlank() && it.children.noneBlank() }
    }


    private fun Set<Field>.anyBlank(canConver: Boolean): Boolean {
        return any { it.isBlank(canConver) || it.children.anyBlank(canConver) }
    }

    private fun Field.isBlank(canConver: Boolean) =
        description.isBlank() || canConver && canCover


    fun Map<String, Any?>.toFields(
        fields: Set<Field>,
        expand: Boolean = false
    ): LinkedHashSet<Field> {
        return this.mapTo(LinkedHashSet()) { (k, v) ->
            val field = fields.field(k, v)
            if (expand) {
                val expandValue = field.value.toMap()
                if (expandValue != null) {
                    field.children = expandValue.toFields(field.children + fields, true)
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
                .apply {
                    partType = if (it.submittedFileName == null) {
                        "text"
                    } else {
                        value = if (value.isNotBlank()) Operation.UNRECORDED_MARK else value
                        "file"
                    }
                }
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
            ?: if (fuzzy) {
                val newName = when {
                    name.endsWith("Name") -> name.substringBeforeLast("Name")
                    name.endsWith("Desc") -> name.substringBeforeLast("Desc")
                    name.endsWith("Path") -> name.substringBeforeLast("Path")
                    name.endsWith("Url") -> name.substringBeforeLast("Url")
                    name.endsWith("Urls") -> name.substringBeforeLast("Urls")
                    name.startsWith("start") -> name.substringAfter("start")
                        .decapitalized()

                    name.endsWith("Start") -> name.substringBeforeLast("Start")
                    name.startsWith("end") -> name.substringAfter("end")
                        .decapitalized()

                    name.endsWith("End") -> name.substringBeforeLast("End")
                    name.endsWith("Pct") -> name.substringBeforeLast("Pct")
                    name.endsWith("Psign") -> name.substringBeforeLast("Psign")
                    name.endsWith("Alurl") -> name.substringBeforeLast("Alurl")
                    name.endsWith("Alurls") -> name.substringBeforeLast("Alurls")
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
                    if (name.endsWith("Psign"))
                        field.description = "播放器签名"
                    if (name.endsWith("Desc"))
                        field.description = "描述"
                    if (name.endsWith("Alurl") || name.endsWith("Alurls"))
                        field.description += "防盗链URL"
                }
                return field
            } else null
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

