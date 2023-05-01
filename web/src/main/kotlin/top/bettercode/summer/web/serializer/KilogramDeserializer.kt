package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import top.bettercode.summer.web.support.KilogramUtil.toGram
import java.io.IOException

/**
 * @author Peter Wu
 */
class KilogramDeserializer : JsonDeserializer<Long>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Long {
        return toGram(p.valueAsString)
    }
}
