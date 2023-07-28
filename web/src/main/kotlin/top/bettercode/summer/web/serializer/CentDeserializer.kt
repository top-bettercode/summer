package top.bettercode.summer.web.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import top.bettercode.summer.tools.lang.util.MoneyUtil.toCent

/**
 * @author Peter Wu
 */
class CentDeserializer : JsonDeserializer<Long>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Long {
        return toCent(p.valueAsString)
    }
}
