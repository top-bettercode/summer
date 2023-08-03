package top.bettercode.summer.test.web.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.util.Assert
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import top.bettercode.summer.web.serializer.annotation.JsonEmbeddedId
import java.io.Serializable

/**
 * @author Peter Wu
 */
class JsonEmbeddedIdTest {
    class User {
        @JsonEmbeddedId
        var key: UserKey? = null
        var tel: String? = null
        var password: String? = null
    }

    class UserKey : Serializable {
        var id: String? = null
        var key: String? = null

        constructor()
        constructor(key: String) {
            Assert.hasText(key, "key不能为空")
            val split = key.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            Assert.isTrue(split.size == 2, "key格式不对")
            id = split[0]
            this.key = split[1]
        }

        override fun toString(): String {
            return "$id,$key"
        }

        companion object
    }

    val objectMapper = ObjectMapper()

    @Test
    fun test() {
        val user = User()
        val key = UserKey("1,2")
        user.key = key
        user.password = "1"
        user.tel = "18000000000"
        val value = objectMapper.writeValueAsString(user)
        System.err.println(value)
        System.err.println(valueOf(objectMapper.readValue(value, User::class.java)))
    }
}