package top.bettercode.summer.test.autodoc

import org.springframework.util.ClassUtils
import org.springframework.util.MultiValueMap
import top.bettercode.summer.test.autodoc.field.FieldDescFix.Companion.findField
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import top.bettercode.summer.tools.autodoc.operation.DocOperationRequest
import top.bettercode.summer.tools.autodoc.operation.DocOperationResponse
import top.bettercode.summer.tools.lang.operation.Operation
import top.bettercode.summer.tools.lang.operation.OperationRequestPart
import top.bettercode.summer.tools.lang.util.StringUtil
import kotlin.text.isNullOrBlank

/**
 *
 * @author Peter Wu
 */
object DocExt {

    fun ext(
        docOperation: DocOperation,
        requiredHeaders: MutableSet<String>,
        requiredParameters: MutableSet<String>,
        defaultValueHeaders: Map<String, String>,
        defaultValueParams: Map<String, String>
    ) {
        val request = docOperation.request as DocOperationRequest
        request.uriVariablesExt = request.uriVariables.toFields(request.uriVariablesExt)
        request.headersExt = request.headers.singleValueMap.toFields(request.headersExt)
        request.headersExt.forEach {
            it.required = requiredHeaders.contains(it.name)
        }

        request.queriesExt =
            request.queries.singleValueMap.toFields(request.queriesExt, expand = true)

        request.queriesExt.forEach {
            setRequired(it, requiredParameters)
        }

        request.parametersExt =
            request.parameters.singleValueMap.toFields(request.parametersExt, expand = true)
        request.parametersExt.forEach {
            setRequired(it, requiredParameters)
        }

        request.partsExt = request.parts.toFields(request.partsExt, expand = true)
        request.partsExt.forEach {
            setRequired(it, requiredParameters)
        }

        request.contentExt =
            request.contentAsString.convert()?.toMap()?.toFields(request.contentExt, expand = true)
                ?: linkedSetOf()
        if (request.contentExt.isEmpty()) {
            if (request.content.isNotEmpty())
                request.content = Operation.UNRECORDED_MARK.toByteArray()
        } else {
            request.contentExt.forEach {
                setRequired(it, requiredParameters)
            }
        }

        if (defaultValueHeaders.isNotEmpty()) {
            request.headersExt.forEach {
                val defaultValueHeader = defaultValueHeaders[it.name]
                if (!defaultValueHeader.isNullOrBlank()) {
                    it.defaultVal = defaultValueHeader
                }
            }
        }
        if (defaultValueParams.isNotEmpty()) {
            request.queriesExt.forEach {
                val defaultValueParam = defaultValueParams[it.name]
                if (!defaultValueParam.isNullOrBlank()) {
                    it.defaultVal = defaultValueParam
                }
            }
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

        val response = docOperation.response as DocOperationResponse
        response.headersExt = response.headers.singleValueMap.toFields(response.headersExt)
        response.contentExt =
            response.contentAsString.convert()?.toMap()
                ?.toFields(response.contentExt, expand = true)
                ?: linkedSetOf()
        if (response.contentExt.isEmpty() && response.content.isNotEmpty()) {
            response.content = Operation.UNRECORDED_MARK.toByteArray()
        }
    }

    private fun Map<String, Any?>.toFields(
        fields: Set<Field>,
        expand: Boolean = false
    ): LinkedHashSet<Field> {
        return this.mapTo(LinkedHashSet()) { (k, v) ->
            fields.field(k, v, expand)
        }
    }


    private fun Set<Field>.field(
        name: String, value: Any?, expand: Boolean = false
    ): Field {
        val type = (value?.type ?: "String")
        val field = findField(name = name, type = type) ?: Field(name = name, type = type)

        var tempVal = value
        if (tempVal == null || "" == tempVal) {
            tempVal = field.defaultVal
        }

        val result = tempVal.convert()
        field.value = result?.let { StringUtil.valueOf(it) } ?: ""

        if (expand) {
            val expandValue = result?.toMap()
            if (expandValue != null) {
                field.children = expandValue.toFields(field.children + this, true)
            } else {
                field.children = LinkedHashSet()
            }
        }

        return field
    }


    private fun Collection<OperationRequestPart>.toFields(
        fields: Set<Field>, expand: Boolean = false
    ): LinkedHashSet<Field> {
        return this.mapTo(
            LinkedHashSet()
        ) {
            fields.field(it.name, it.contentAsString, expand)
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


    private fun setRequired(
        field: Field,
        requiredParameters: MutableSet<String>,
        prefix: String = ""
    ) {
        field.required = requiredParameters.contains(prefix + field.name)
        if (field.children.isNotEmpty()) {
            field.children.forEach {
                setRequired(it, requiredParameters, "${prefix + field.name}.")
            }
        }
    }

    private val MultiValueMap<String, String>.singleValueMap: Map<String, String>
        get() {
            return this.mapValues { it.value.joinToString(",") }
        }

    /**
     * 尝试转换字符串为对象
     */
    private fun Any.toMap(): Map<String, Any?>? {
        val result = this
        if (result is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            return result as Map<String, Any?>?
        } else if (result is List<*> && result.isNotEmpty()) {
            val first = result.first()
            @Suppress("UNCHECKED_CAST")
            if (first is Map<*, *>)
                return first as Map<String, Any?>?
        }
        return null
    }


    /**
     * @param unwrapped 解析字段
     */
    private fun Any.convert(): Any? {
        if (this is List<*> && this.isNotEmpty()) {
            val map = mutableMapOf<Any?, Any?>()
            this.forEach {
                val convertAny = it?.convert()
                if (convertAny is Map<*, *>) {
                    convertAny.forEach { (any, u) ->
                        val value = map[any]
                        if (value == null) {
                            map[any] = u?.convert()
                        } else if (isEmpty(value) && !isEmpty(u)) {
                            map[any] = u?.convert()
                        }
                    }
                }
            }
            return if (map.isEmpty()) {
                listOf(this.firstOrNull { it != null }?.convert())
            } else
                listOf(map)
        } else if (this is Map<*, *>) {
            return this.mapValues { it.value?.convert() }
        } else if (this is String) {
            return if (this.isBlank()) {
                this
            } else {
                try {
                    StringUtil.objectMapper().readValue(this, Map::class.java).convert()
                } catch (ignore: Exception) {
                    try {
                        StringUtil.objectMapper().readValue(this, List::class.java).convert()
                    } catch (e: Exception) {
                        return this
                    }
                }
            }
        } else
            return this
    }

    private fun isEmpty(value: Any?) =
        value == null || (value is Collection<*> && value.isEmpty()) || (value is Array<*> && value.isEmpty())


    private val Any.type: String
        get() {
            if (this::class.java == String::class.java) {
                return "String"
            } else if (ClassUtils.isPrimitiveOrWrapper(this::class.java)) {
                return this::class.java.simpleName
            } else if (this::class.java.isArray || (Collection::class.java.isAssignableFrom(this::class.java) && !Map::class.java.isAssignableFrom(
                    this::class.java
                ))
            ) {
                return "Array"
            }
            return "Object"
        }

}