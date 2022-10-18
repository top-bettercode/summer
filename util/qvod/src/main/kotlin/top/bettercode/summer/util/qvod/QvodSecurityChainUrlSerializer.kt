package top.bettercode.summer.util.qvod

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import top.bettercode.simpleframework.support.ApplicationContextHolder
import java.io.IOException
import java.util.*

/**
 *
 * @author Peter Wu
 */
@JacksonStdImpl
class QvodSecurityChainUrlSerializer : StdScalarSerializer<String>(
    String::class.java, false
) {
    private val qvodClient: QvodClient

    init {
        qvodClient = ApplicationContextHolder.getBean(QvodClient::class.java)
    }

    @Throws(IOException::class)
    override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(qvodClient.securityChainUrl(value))
    }

    @Throws(IOException::class)
    override fun serializeWithType(
        value: String, gen: JsonGenerator, provider: SerializerProvider, typeSer: TypeSerializer
    ) {
        serialize(value, gen, provider)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}