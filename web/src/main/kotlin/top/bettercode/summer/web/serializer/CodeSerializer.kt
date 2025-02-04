package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.summer.web.serializer.annotation.JsonCode
import top.bettercode.summer.web.support.code.CodeServiceHolder
import java.io.Serializable

/**
 * code name Serializer
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class CodeSerializer(
    private val codeServiceRef: String?,
    private val codeType: String,
    private val useExtensionField: Boolean
) : StdScalarSerializer<Serializable>(Serializable::class.java, false), ContextualSerializer {
    @JvmOverloads
    constructor(codeType: String = "", useExtensionField: Boolean = true) : this(
        "",
        codeType,
        useExtensionField
    )

    override fun serialize(value: Serializable, gen: JsonGenerator, provider: SerializerProvider) {
        val outputContext = gen.outputContext
        val fieldName = outputContext.currentName
        val codeName: String
        val trueCodeType = getCodeType(fieldName)
        codeName = if (value is String && value.contains(",")) {
            value.split(",").joinToString(",") {
                getName(trueCodeType, it)
            }
        } else {
            getName(trueCodeType, value)
        }
        if (useExtensionField) {
            gen.writeObject(value)
            gen.writeStringField(fieldName + "Name", codeName)
        } else {
            gen.writeString(codeName)
        }
    }

    private fun getName(codeType: String, code: Serializable): String {
        return CodeServiceHolder.get(codeServiceRef, codeType)?.getName(code) ?: code.toString()
    }

    private fun getCodeType(fieldName: String): String {
        return if ("" == codeType) {
            fieldName
        } else codeType
    }

    override fun createContextual(
        prov: SerializerProvider,
        property: BeanProperty?
    ): JsonSerializer<*> {
        if (property != null) {
            val annotation = property.getAnnotation(JsonCode::class.java)
            var codeType = annotation.value
            codeType = if ("" == codeType) property.name else codeType
            return CodeSerializer(annotation.codeServiceRef, codeType, annotation.extended)
        }
        return prov.findNullValueSerializer(null)
    }

    override fun serializeWithType(
        value: Serializable, gen: JsonGenerator,
        provider: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        serialize(value, gen, provider)
    }

}