package top.bettercode.summer.test.autodoc.field

import org.slf4j.LoggerFactory
import top.bettercode.summer.test.autodoc.Autodoc
import top.bettercode.summer.tools.autodoc.AutodocUtil.checkBlank
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import top.bettercode.summer.tools.autodoc.operation.DocOperationRequest
import top.bettercode.summer.tools.autodoc.operation.DocOperationResponse
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.lang.decapitalized
import top.bettercode.summer.tools.lang.property.PropertiesSource
import kotlin.collections.component1
import kotlin.collections.component2

/**
 *
 * @author Peter Wu
 */
abstract class FieldDescFix {

    open val cover: Boolean = false
    open val fixChildren: Boolean = true

    abstract fun descFields(properties: DocProperties): Iterable<Iterable<Field>>

    fun fix(descFields: Iterable<Field>, fields: Iterable<Field>): Boolean {
        fields.blankField().forEach { field ->
            var findField = descFields.findField(field.name, field.type)
            if (field.description.isBlank() && (findField == null || findField.description.isBlank())) {
                findField = descFields.findExtField(field.name, field.type)
            }
            if (findField != null) {
                if (field.isBlank()) {
                    field.canCover = findField.canCover

                    if (field.defaultVal.isBlank() && findField.defaultVal.isNotBlank())
                        field.defaultVal = findField.defaultVal

                    if (findField.type.isNotBlank())
                        field.type = findField.type
                    if (findField.description.isNotBlank())
                        field.description = findField.description
                }
                val children = findField.children
                if (children.isNotEmpty()) {
                    fix(children, field.children)
                }
            }
            fieldDescBundle.all().forEach { (k, v) ->
                field.description = field.description.replace(k, v)
            }
            if (fixChildren)
                fix(descFields, field.children)
        }
        return !fields.anyBlank()
    }

    private fun Iterable<Field>.blankField(): Iterable<Field> {
        return filter {
            it.isBlank() || it.children.anyBlank()
        }
    }

    private fun Iterable<Field>.anyBlank(): Boolean {
        return any { it.isBlank() || it.children.anyBlank() }
    }

    private fun Field.isBlank() = description.isBlank() || cover && canCover

    companion object {
        private val log = LoggerFactory.getLogger(FieldDescFix::class.java)
        private val contentWrapFields: Set<String> =
            setOf("status", "message", "data", "trace", "errors")

        private val fieldDescBundle: PropertiesSource by lazy { PropertiesSource.of("field-desc-replace") }

        fun fix(
            operation: DocOperation,
            extension: GeneratorExtension
        ) {
            val request = operation.request as DocOperationRequest
            val response = operation.response as DocOperationResponse

            val tableFix = TableFix(extension, Autodoc.tableNames)
            val docFieldDescFixes = listOf(
                CommoYmlFix(),
                AutodocFieldFix(),
                MessageFix(),
                DicCodeFix(),
                object : FieldDescFix() {
                    override val cover: Boolean = true

                    override fun descFields(properties: DocProperties): Iterable<Iterable<Field>> {
                        val namedFields = tableFix.namedFields
                        return namedFields
                    }
                },
                object : FieldDescFix() {
                    override fun descFields(properties: DocProperties): Iterable<Iterable<Field>> {
                        val otherFields = tableFix.otherFields
                        return otherFields
                    }
                },
            )

            out@ for (fixDocFieldDesc in docFieldDescFixes) {
                val headerFields = fixDocFieldDesc.descFields(DocProperties.REQUEST_HEADERS)
                for (fields in headerFields) {
                    if (fixDocFieldDesc.fix(fields, request.headersExt)) {
                        break@out
                    }
                }
            }
            request.headersExt.checkBlank("request.headersExt")
            out@ for (fixDocFieldDesc in docFieldDescFixes) {
                val parameterFields =
                    fixDocFieldDesc.descFields(DocProperties.REQUEST_PARAMETERS)
                for (fields in parameterFields) {
                    if (fixDocFieldDesc.fix(fields, request.uriVariablesExt)) {
                        break@out
                    }
                }
            }
            request.uriVariablesExt.checkBlank("request.uriVariablesExt")
            out@ for (fixDocFieldDesc in docFieldDescFixes) {
                val parameterFields =
                    fixDocFieldDesc.descFields(DocProperties.REQUEST_PARAMETERS)
                for (fields in parameterFields) {
                    if (fixDocFieldDesc.fix(fields, request.queriesExt)) {
                        break@out
                    }
                }
            }
            request.queriesExt.checkBlank("request.queriesExt")
            out@ for (fixDocFieldDesc in docFieldDescFixes) {
                val parameterFields =
                    fixDocFieldDesc.descFields(DocProperties.REQUEST_PARAMETERS)
                for (fields in parameterFields) {
                    if (fixDocFieldDesc.fix(fields, request.parametersExt)) {
                        break@out
                    }
                }
            }
            request.parametersExt.checkBlank("request.parametersExt")
            out@ for (fixDocFieldDesc in docFieldDescFixes) {
                val parameterFields =
                    fixDocFieldDesc.descFields(DocProperties.REQUEST_PARAMETERS)
                for (fields in parameterFields) {
                    if (fixDocFieldDesc.fix(fields, request.partsExt)) {
                        break@out
                    }
                }
            }
            request.partsExt.checkBlank("request.partsExt")
            out@ for (fixDocFieldDesc in docFieldDescFixes) {
                val parameterFields =
                    fixDocFieldDesc.descFields(DocProperties.REQUEST_PARAMETERS)
                for (fields in parameterFields) {
                    if (fixDocFieldDesc.fix(fields, request.contentExt)) {
                        break@out
                    }
                }
            }
            request.contentExt.checkBlank("request.contentExt")

            out@ for (fixDocFieldDesc in docFieldDescFixes) {
                val responseFields = fixDocFieldDesc.descFields(DocProperties.RESPONSE_CONTENT)
                for (fields in responseFields) {
                    if (fixDocFieldDesc.fix(fields, response.contentExt)) {
                        break@out
                    }
                }
            }
            response.contentExt.checkBlank("response.contentExt")
        }

        fun Iterable<Field>.findExtField(
            name: String,
            type: String
        ): Field? {
            val newName = when {
                name.endsWith("Name") -> name.substringBeforeLast("Name")
                name.endsWith("Desc") -> name.substringBeforeLast("Desc")
                name.endsWith("Path") -> name.substringBeforeLast("Path")
                name.endsWith("Url") -> name.substringBeforeLast("Url")
                name.endsWith("Urls") -> name.substringBeforeLast("Urls")
                name.endsWith("Pct") -> name.substringBeforeLast("Pct")
                name.endsWith("Psign") -> name.substringBeforeLast("Psign")
                name.endsWith("Alurl") -> name.substringBeforeLast("Alurl")
                name.endsWith("Alurls") -> name.substringBeforeLast("Alurls")

                name.startsWith("start") -> name.substringAfter("start").decapitalized()
                name.endsWith("Start") -> name.substringBeforeLast("Start")
                name.startsWith("end") -> name.substringAfter("end").decapitalized()
                name.endsWith("End") -> name.substringBeforeLast("End")
                else -> {
                    return null
                }
            }
            val field = this.findField(newName, type)
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
        }

        fun Iterable<Field>.findField(name: String, type: String): Field? {
            val set = this
            val field =
                set.find { it.name == name && it.type.equals(type, true) }?.copy()
                    ?: set.find { it.name.equals(name, true) && it.type.equals(type, true) }?.copy()
                    ?: set.find { it.name == name }?.copy() ?: set.find {
                        it.name.equals(
                            name,
                            true
                        )
                    }?.copy()
            return field?.apply { this.name = name }
        }

    }
}