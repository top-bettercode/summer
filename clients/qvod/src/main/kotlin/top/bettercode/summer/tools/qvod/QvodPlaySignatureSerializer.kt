package top.bettercode.summer.tools.qvod

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.summer.web.support.ApplicationContextHolder

/**
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class QvodPlaySignatureSerializer : StdScalarSerializer<String>(
        String::class.java, false
) {
    private val qvodClient: QvodClient = ApplicationContextHolder.getBean(QvodClient::class.java)!!

    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObject(value)
        val fileIdName = gen.outputContext.currentName
        var playSignature = ""
        var appId: Long? = null
        if (value.isNotBlank()) {
            playSignature = qvodClient.playSignature(value)
            appId = qvodClient.properties.appId
        }
        gen.writeStringField(fileIdName + "Psign", playSignature)
        gen.writeObjectField("qvodAppID", appId)
    }

    override fun serializeWithType(
            value: String, gen: JsonGenerator, provider: SerializerProvider, typeSer: TypeSerializer
    ) {
        serialize(value, gen, provider)
    }

}