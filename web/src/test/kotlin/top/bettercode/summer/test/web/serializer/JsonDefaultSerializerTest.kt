package top.bettercode.summer.test.web.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment
import top.bettercode.summer.test.web.DataDicBean
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.serializer.CustomNullSerializerModifier
import top.bettercode.summer.web.serializer.UrlSerializer.Companion.setEnvironment
import top.bettercode.summer.web.serializer.annotation.JsonDefault
import java.math.BigDecimal

/**
 * @author Peter Wu
 */
class JsonDefaultSerializerTest {
    private val jacksonExtProperties = JacksonExtProperties()
    val objectMapper = ObjectMapper()
    @BeforeEach
    fun setUp() {
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.setSerializerFactory(objectMapper.serializerFactory
                .withSerializerModifier(CustomNullSerializerModifier(jacksonExtProperties)))
        val mockEnvironment = MockEnvironment()
        //    mockEnvironment.setProperty("summer.multipart.file-url-format", "/path%s");
        mockEnvironment.setProperty("summer.multipart.file-url-format", "http://127.0.0.1%s")
        mockEnvironment.setProperty("path1-url", "http://127.0.0.2%s")
        setEnvironment(mockEnvironment)
    }

    @Test
    fun serialize() {
        objectMapper.addMixIn(DataDicBean::class.java, DataDicBeanMin::class.java)
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.setSerializerFactory(objectMapper.serializerFactory
                .withSerializerModifier(CustomNullSerializerModifier(jacksonExtProperties)))
        var dicBean = DataDicBean()
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"price\":0,\"path\":\"/default.jpg\",\"pathUrl\":\"http://127.0.0.1/default.jpg\",\"path1\":\"\"}",
                objectMapper.writeValueAsString(dicBean))
        dicBean.number1 = BigDecimal(100)
        dicBean.path = "/a.jpg"
        dicBean.path1 = "/b.jpg"
        Assertions.assertEquals(
                "{\"number1\":100,\"number22\":null,\"price\":0,\"path\":\"/a.jpg\",\"pathUrl\":\"http://127.0.0.1/a.jpg\",\"path1\":\"/b.jpg\"}",
                objectMapper.writeValueAsString(dicBean))
        dicBean = DataDicBean()
        dicBean.number2 = BigDecimal(10)
        Assertions.assertEquals(
                "{\"number1\":0,\"number2\":10.000,\"number22\":10,\"price\":0,\"path\":\"/default.jpg\",\"pathUrl\":\"http://127.0.0.1/default.jpg\",\"path1\":\"\"}",
                objectMapper.writeValueAsString(dicBean))
    }

    @Test
    fun defSerialize() {
        val objectMapper = ObjectMapper()
        objectMapper.addMixIn(DataDicBean::class.java, DataDicBeanMin::class.java)
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.setSerializerFactory(objectMapper.serializerFactory
                .withSerializerModifier(CustomNullSerializerModifier(jacksonExtProperties)))
        val dicBean = DataDicBean()
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"price\":0,\"path\":\"/default.jpg\",\"pathUrl\":\"http://127.0.0.1/default.jpg\",\"path1\":\"\"}",
                objectMapper.writeValueAsString(dicBean))
        dicBean.price = 100L
        dicBean.path = "/a.jpg"
        dicBean.path1 = "/b.jpg"
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"price\":\"1.00\",\"path\":\"/a.jpg\",\"pathUrl\":\"http://127.0.0.1/a.jpg\",\"path1\":\"/b.jpg\"}",
                objectMapper.writeValueAsString(dicBean))
    }

    internal interface DataDicBeanMin {
        @get:JsonDefault
        val path1: String?

        @get:JsonDefault("/default.jpg")
        val path: String?

        @get:JsonDefault("0")
        val price: Long?
    }
}