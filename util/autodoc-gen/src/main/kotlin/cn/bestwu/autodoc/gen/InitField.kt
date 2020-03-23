package cn.bestwu.autodoc.gen

import cn.bestwu.autodoc.core.*
import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.autodoc.core.operation.DocOperation
import cn.bestwu.autodoc.core.operation.DocOperationRequest
import cn.bestwu.autodoc.core.operation.DocOperationResponse
import cn.bestwu.generator.DataType
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.generator.database.domain.Table
import cn.bestwu.generator.powerdesigner.PdmReader
import cn.bestwu.generator.puml.PumlConverter
import cn.bestwu.logging.operation.OperationRequestPart
import org.atteo.evo.inflector.English

/**
 *
 * @author Peter Wu
 */
object InitField {

    private val contentWrapFields: Set<String> = setOf("status", "message", "data", "trace", "errors")

    fun init(operation: DocOperation, extension: GeneratorExtension, allTables: Boolean, wrap: Boolean) {
        val request = operation.request as DocOperationRequest
        val response = operation.response as DocOperationResponse

        val uriNeedFix = request.uriVariablesExt.filter { it.description.isBlank() }.toSet()
        val reqHeadNeedFix = request.headersExt.filter { it.description.isBlank() }.toSet()
        val paramNeedFix = request.parametersExt.filter { it.description.isBlank() || it.canCover }.toSet()
        val partNeedFix = request.partsExt.filter { it.description.isBlank() }.toSet()
        val reqContentNeedFix = request.contentExt.filter { it.description.isBlank() || it.canCover }.toSet()
        val resHeadNeedFix = response.headersExt.filter { it.description.isBlank() }.toSet()
        val resContentNeedFix = response.contentExt.filter {
            val cover = it.description.isBlank() || it.canCover
            if (wrap)
                cover && !contentWrapFields.contains(it.name)
            else cover
        }.toSet()
        if (uriNeedFix.isNotEmpty() || reqHeadNeedFix.isNotEmpty() || paramNeedFix.isNotEmpty() || partNeedFix.isNotEmpty() || reqContentNeedFix.isNotEmpty() || resHeadNeedFix.isNotEmpty() || resContentNeedFix.isNotEmpty()) {
            extension.fixFields(allTables) { fields ->
                fields.fix(uriNeedFix)
                fields.fix(reqHeadNeedFix)
                fields.fix(paramNeedFix)
                fields.fix(partNeedFix)
                fields.fix(reqContentNeedFix)
                fields.fix(resHeadNeedFix)
                fields.fix(resContentNeedFix)

                uriNeedFix.none { it.description.isBlank() } && reqHeadNeedFix.none { it.description.isBlank() } && paramNeedFix.none { it.description.isBlank() } && partNeedFix.none { it.description.isBlank() } && reqContentNeedFix.none { it.description.isBlank() } && resHeadNeedFix.none { it.description.isBlank() } && resContentNeedFix.none { it.description.isBlank() }
            }
        }

        request.uriVariablesExt = request.uriVariablesExt.sorted().toSortedSet()
        request.uriVariablesExt.filter { it.description.isBlank() }.forEach { System.err.println("[request.uriVariablesExt]未找到字段[${it.name}]的描述") }
        request.headersExt = request.headersExt.sorted().toSortedSet()
        request.headersExt.filter { it.description.isBlank() }.forEach { System.err.println("[request.headersExt]未找到字段[${it.name}]的描述") }
        request.parametersExt = request.parametersExt.sorted().toSortedSet()
        request.parametersExt.filter { it.description.isBlank() }.forEach { System.err.println("[request.parametersExt]未找到字段[${it.name}]的描述") }
        request.partsExt = request.partsExt.sorted().toSortedSet()
        request.partsExt.filter { it.description.isBlank() }.forEach { System.err.println("[request.partsExt]未找到字段[${it.name}]的描述") }
        request.contentExt = request.contentExt.sorted().toSortedSet()
        request.contentExt.filter { it.description.isBlank() }.forEach { System.err.println("[request.contentExt]未找到字段[${it.name}]的描述") }
        response.headersExt = response.headersExt.sorted().toSortedSet()
        response.headersExt.filter { it.description.isBlank() }.forEach { System.err.println("[response.headersExt]未找到字段[${it.name}]的描述") }
        response.contentExt = response.contentExt.sorted().toSortedSet()
        response.contentExt.filter { it.description.isBlank() }.forEach { System.err.println("[response.contentExt]未找到字段[${it.name}]的描述") }
    }

    private fun Set<Field>.fix(needFixFields: Set<Field>) {
        needFixFields.forEach {
            this.fixField(it)
        }
        needFixFields.knewFixFields(needFixFields)
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
                    ?: "", "", column.nullable)
        }
        fields.addAll(fields.map { Field(English.plural(it.name), "Array", it.description) })
        fields.add(Field(entityName(extension), "Object", remarks))
        fields.add(Field(pathName(extension), "Array", remarks))
        return fields
    }
}

fun Map<String, Any?>.toFields(fields: Set<Field>, expand: Boolean = false, prefix: String = ""): List<Field> {
    val results = mutableListOf<Field>()
    this.forEach { (k, v) ->
        val key = prefix + k
        val field = fields.field(key, v)
        results.add(field)
        if (expand && field.expanded) {
            val expandValue = field.value.toMap()
            if (expandValue != null) {
                results.addAll(expandValue.toFields(fields, expand, "$key."))
            }
        }
    }
    return results
}

fun Collection<OperationRequestPart>.toFields(fields: Set<Field>): List<Field> {
    return this.map { fields.field(it.name, it.contentAsString) }
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


