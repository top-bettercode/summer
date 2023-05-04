package top.bettercode.summer.test.web.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.summer.test.web.StringArrayBean

/**
 * @author Peter Wu
 */
internal class ArraySerializerTest {
    val objectMapper = ObjectMapper()
    @BeforeEach
    fun setUp() {
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
    }

    @Test
    @Throws(Exception::class)
    fun serialize() {
        val bean = StringArrayBean()
        bean.setAry("a,b,c,")
        val actual = objectMapper.writeValueAsString(bean)
        System.err.println(actual)
        Assertions.assertEquals("{\"ary\":\"a,b,c,\",\"aryArray\":[\"a\",\"b\",\"c\"]}", actual)
    }

    @Test
    @Throws(Exception::class)
    fun serialize1() {
        val bean = StringArrayBean()
        bean.setAry("a,b,c")
        val actual = objectMapper.writeValueAsString(bean)
        System.err.println(actual)
        Assertions.assertEquals("{\"ary\":\"a,b,c\",\"aryArray\":[\"a\",\"b\",\"c\"]}", actual)
    }
}