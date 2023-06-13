package top.bettercode.summer.test.web.serializer

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import top.bettercode.summer.web.serializer.annotation.JsonHide

/**
 * @author Peter Wu
 */
class HideSerializerTest {
    internal class User {
        @JsonHide(beginKeep = 2, endKeep = 2)
        var tel: String? = null

        @JsonHide(beginKeep = 1, endKeep = 1)
        var password: String? = null
    }

    val objectMapper = ObjectMapper()
    @Test
    fun test() {
        val user = User()
        user.password = "1"
        user.tel = "18000000000"
        System.err.println(valueOf(objectMapper.writeValueAsString(user)))
    }
}