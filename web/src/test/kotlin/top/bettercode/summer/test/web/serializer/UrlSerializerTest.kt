package top.bettercode.summer.test.web.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment
import top.bettercode.summer.tools.lang.operation.PrettyPrintingContentModifier.modifyContent
import top.bettercode.summer.web.properties.JacksonExtProperties
import top.bettercode.summer.web.serializer.CustomNullSerializerModifier
import top.bettercode.summer.web.serializer.UrlSerializer.Companion.convert
import top.bettercode.summer.web.serializer.UrlSerializer.Companion.setEnvironment
import top.bettercode.summer.web.serializer.annotation.JsonUrl

/**
 * @author Peter Wu
 */
class UrlSerializerTest {
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
    fun convert() {
        System.err.println(convert("/adb"))
    }

    @Test
    fun serializeExtend() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMinExtend::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val path = "/abc.jpg"
        dicBean.path = path
        dicBean.path1 = path
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.1/abc.jpg\",\"path1\":\"/abc.jpg\",\"path1Url\":\"http://127.0.0.2/abc.jpg\"}",
                objectMapper.writeValueAsString(dicBean))
        dicBean.path = ""
        dicBean.path = "  "
        dicBean.path1 = null
        Assertions.assertEquals("{\"number1\":0,\"number22\":null,\"path\":\"\",\"pathUrl\":\"\"}",
                objectMapper.writeValueAsString(dicBean))
    }

    @Test
    fun serialize() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMin::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val path = "/abc.jpg"
        dicBean.path = path
        dicBean.path1 = path
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"path\":\"http://127.0.0.1/abc.jpg\",\"path1\":\"http://127.0.0.2/abc.jpg\"}",
                objectMapper.writeValueAsString(dicBean))
        dicBean.path = ""
        dicBean.path = "  "
        dicBean.path1 = null
        Assertions.assertEquals("{\"number1\":0,\"number22\":null,\"path\":\"\"}",
                objectMapper.writeValueAsString(dicBean))
    }

    @Test
    fun serializeArrayStringExtend() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMinStringExtend::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val path = "/abc.jpg,/124.jpg"
        dicBean.path = path
        dicBean.path1 = path
        System.err.println(objectMapper.writeValueAsString(dicBean))
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"path\":\"/abc.jpg,/124.jpg\",\"pathUrls\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/124.jpg\"],\"path1\":\"/abc.jpg,/124.jpg\",\"path1Urls\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/124.jpg\"]}",
                objectMapper.writeValueAsString(dicBean))
    }

    @Test
    fun serializeArrayString() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMinString::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val path = "/abc.jpg,/124.jpg"
        dicBean.path = path
        dicBean.path1 = path
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"path\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/124.jpg\"],\"path1\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/124.jpg\"]}",
                objectMapper.writeValueAsString(dicBean))
    }

    @Test
    fun serializeArrayStringExtendAsMap() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMinStringExtendAsMap::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val path = "/abc.jpg,/124.jpg"
        dicBean.path = path
        dicBean.path1 = path
        val actual = objectMapper.writeValueAsString(dicBean)
        System.err.println(actual)
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"path\":\"/abc.jpg,/124.jpg\",\"pathUrls\":[{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.1/abc.jpg\"},{\"path\":\"/124.jpg\",\"pathUrl\":\"http://127.0.0.1/124.jpg\"}],\"path1\":\"/abc.jpg,/124.jpg\",\"path1Urls\":[{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.2/abc.jpg\"},{\"path\":\"/124.jpg\",\"pathUrl\":\"http://127.0.0.2/124.jpg\"}]}",
                actual)
    }

    private fun prettyStr(actual: String): String {
        return modifyContent(actual)
    }

    @Test
    fun serializeArrayStringAsMap() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMinStringAsMap::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val path = "/abc.jpg,/124.jpg"
        dicBean.path = path
        dicBean.path1 = path
        val actual = objectMapper.writeValueAsString(dicBean)
        System.err.println(actual)
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"path\":[{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.1/abc.jpg\"},{\"path\":\"/124.jpg\",\"pathUrl\":\"http://127.0.0.1/124.jpg\"}],\"path1\":[{\"path\":\"/abc.jpg\",\"pathUrl\":\"http://127.0.0.2/abc.jpg\"},{\"path\":\"/124.jpg\",\"pathUrl\":\"http://127.0.0.2/124.jpg\"}]}",
                actual)
    }

    @Test
    fun serializeArrayExtend() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMinExtend::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val paths = ArrayList<String>()
        paths.add("/abc.jpg")
        paths.add("/def.jpg")
        paths.add(" ")
        dicBean.pathArray = paths.toTypedArray<String>()
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"pathArray\":[\"/abc.jpg\",\"/def.jpg\",\" \"],\"pathArrayUrls\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/def.jpg\"],\"pathArray1\":[\"/abc.jpg\",\"/def.jpg\",\" \"],\"pathArray1Urls\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/def.jpg\"]}",
                objectMapper.writeValueAsString(dicBean))
        dicBean.pathArray = null
        Assertions.assertEquals("{\"number1\":0,\"number22\":null}", objectMapper.writeValueAsString(dicBean))
    }

    @Test
    fun serializeArray() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMin::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val paths = ArrayList<String>()
        paths.add("/abc.jpg")
        paths.add("/def.jpg")
        paths.add(" ")
        dicBean.pathArray = paths.toTypedArray<String>()
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"pathArray\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/def.jpg\"],\"pathArray1\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/def.jpg\"]}",
                objectMapper.writeValueAsString(dicBean))
        dicBean.pathArray = null
        Assertions.assertEquals("{\"number1\":0,\"number22\":null}", objectMapper.writeValueAsString(dicBean))
    }

    @Test
    fun serializeCollectionExtend() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMinExtend::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val paths = mutableListOf<String>()
        paths.add("/abc.jpg")
        paths.add("/def.jpg")
        paths.add(" ")
        dicBean.paths = paths
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"paths\":[\"/abc.jpg\",\"/def.jpg\",\" \"],\"pathsUrls\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/def.jpg\"],\"paths1\":[\"/abc.jpg\",\"/def.jpg\",\" \"],\"paths1Urls\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/def.jpg\"]}",
                objectMapper.writeValueAsString(dicBean))
        dicBean.paths = null
        Assertions.assertEquals("{\"number1\":0,\"number22\":null}", objectMapper.writeValueAsString(dicBean))
    }

    @Test
    fun serializeCollection() {
        objectMapper.addMixIn(top.bettercode.summer.test.web.DataDicBean::class.java, DataDicBeanMin::class.java)
        val dicBean = top.bettercode.summer.test.web.DataDicBean()
        val paths = mutableListOf<String>()
        paths.add("/abc.jpg")
        paths.add("/def.jpg")
        paths.add(" ")
        dicBean.paths = paths
        Assertions.assertEquals(
                "{\"number1\":0,\"number22\":null,\"paths\":[\"http://127.0.0.1/abc.jpg\",\"http://127.0.0.1/def.jpg\"],\"paths1\":[\"http://127.0.0.2/abc.jpg\",\"http://127.0.0.2/def.jpg\"]}",
                objectMapper.writeValueAsString(dicBean))
        dicBean.paths = null
        Assertions.assertEquals("{\"number1\":0,\"number22\":null}", objectMapper.writeValueAsString(dicBean))
    }

    internal interface DataDicBeanMinExtend {
        @get:JsonUrl
        val paths: List<String?>?

        @get:JsonUrl("\${path1-url}")
        val paths1: List<String?>?

        @get:JsonUrl
        val pathArray: Array<String?>?

        @get:JsonUrl("\${path1-url}")
        val pathArray1: Array<String?>?

        @get:JsonUrl
        val path: String?

        @get:JsonUrl("\${path1-url}")
        val path1: String?
    }

    internal interface DataDicBeanMinStringExtend {
        @get:JsonUrl(separator = ",")
        val path: String?

        @get:JsonUrl(value = "\${path1-url}", separator = ",")
        val path1: String?
    }

    internal interface DataDicBeanMinStringExtendAsMap {
        @get:JsonUrl(separator = ",", asMap = true)
        val path: String?

        @get:JsonUrl(value = "\${path1-url}", separator = ",", asMap = true)
        val path1: String?
    }

    internal interface DataDicBeanMinString {
        @get:JsonUrl(separator = ",", extended = false)
        val path: String?

        @get:JsonUrl(value = "\${path1-url}", separator = ",", extended = false)
        val path1: String?
    }

    internal interface DataDicBeanMinStringAsMap {
        @get:JsonUrl(separator = ",", extended = false, asMap = true)
        val path: String?

        @get:JsonUrl(value = "\${path1-url}", separator = ",", extended = false, asMap = true)
        val path1: String?
    }

    internal interface DataDicBeanMin {
        @get:JsonUrl(extended = false)
        val paths: List<String?>?

        @get:JsonUrl(value = "\${path1-url}", extended = false)
        val paths1: List<String?>?

        @get:JsonUrl(extended = false)
        val pathArray: Array<String?>?

        @get:JsonUrl(value = "\${path1-url}", extended = false)
        val pathArray1: Array<String?>?

        @get:JsonUrl(extended = false)
        val path: String?

        @get:JsonUrl(value = "\${path1-url}", extended = false)
        val path1: String?
    }
}