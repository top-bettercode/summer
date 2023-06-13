package top.bettercode.summer.test.web.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.Test
import top.bettercode.summer.web.serializer.annotation.JsonRawValuePlus

/**
 * @author Peter Wu
 * @since 0.1.15
 */
class RawValuePlusSerializerTest {
    @Test
    fun serialize() {
        val objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        var string = objectMapper.writeValueAsString(BeanWithRaw("张三", null))
        System.err.println(string)
        string = objectMapper.writeValueAsString(BeanWithRaw("张三", " "))
        System.err.println(string)
        string = objectMapper
                .writeValueAsString(BeanWithRaw("张三", "{\"qty\":10.000,\"payAmount\":26900.00}"))
        System.err.println(string)
    }

    internal class BeanWithRaw(var name: String, @field:JsonRawValuePlus var raw: String?)
}