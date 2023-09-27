package top.bettercode.summer.test

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.StreamUtils
import org.springframework.web.context.WebApplicationContext
import top.bettercode.summer.logging.RequestLoggingFilter
import top.bettercode.summer.logging.RequestLoggingProperties
import top.bettercode.summer.test.autodoc.Autodoc
import top.bettercode.summer.test.autodoc.Autodoc.requiredParameters
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.web.BaseController
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.io.File
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.function.Supplier

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
@Suppress("SpringBootApplicationProperties")
@SpringBootTest
@TestPropertySource(properties = ["summer.security.enabled=false", "seata.enabled=false"])
class BaseWebNoAuthTest : MockMvcRequestBuilders() {

    @JvmField
    protected final val log: Logger = LoggerFactory.getLogger(javaClass)

    protected lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var requestLoggingFilter: RequestLoggingFilter

    @Autowired(required = false)
    private lateinit var autoDocFilter: AutoDocFilter

    @Autowired
    private lateinit var webProperties: SummerWebProperties

    @Autowired
    private lateinit var errorController: BasicErrorController

    @Autowired
    protected lateinit var requestLoggingProperties: RequestLoggingProperties

    @JvmField
    protected final val objectMapper: ObjectMapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        //--------------------------------------------
        requestLoggingProperties.isForceRecord = true
        requestLoggingProperties.isIncludeRequestBody = true
        requestLoggingProperties.isIncludeResponseBody = true
        requestLoggingProperties.isFormat = true
        mockMvc = mockMvcBuilder().build()
        defaultBeforeEach()
        System.err.println("------------------------------------------------------")
    }

    protected fun defaultBeforeEach() {
    }

    protected fun beforeEach() {
    }

    protected fun embeddedDatabase(): Boolean {
        return ApplicationContextHolder.getProperty("spring.datasource.url").isNullOrBlank()
    }

    protected fun notFound(): Supplier<out RuntimeException?> {
        return BaseController.notFound()
    }

    protected fun notFound(msg: String?): Supplier<out RuntimeException?> {
        return BaseController.notFound(msg)
    }

    protected fun mockMvcBuilder(): DefaultMockMvcBuilder {
        return MockMvcBuilders.webAppContextSetup(context)
                .addFilter<DefaultMockMvcBuilder>(autoDocFilter)
                .addFilter<DefaultMockMvcBuilder>(requestLoggingFilter)
                .addFilter(TestErrorPageFilter(errorController, webProperties))
    }

    protected fun contentStatusIsOk(): ResultMatcher {
        return ResultMatcher { result: MvcResult ->
            var status = contentAsJsonNode(result)["status"]?.asInt()
            if (status == null) {
                status = result.response.status
            }
            Assertions.assertTrue(status < 400)
        }
    }

    protected fun contentStatus(status: Int): ResultMatcher {
        return ResultMatcher { result: MvcResult -> Assertions.assertEquals(contentAsJsonNode(result)["status"]?.asInt(), status) }
    }

    protected fun contentAsJsonNode(result: MvcResult): JsonNode {
        return objectMapper.readTree(result.response.contentAsByteArray)
    }

    private fun getFileName(result: MvcResult): String? {
        var contentDisposition = result.response.getHeader("Content-Disposition")
        return if (contentDisposition != null) {
            contentDisposition = URLDecoder.decode(contentDisposition.replace("UTF-8''", "").replace(".*filename\\*?=(.*?)".toRegex(), "$1").trim('"'), "UTF-8")
            "build/$contentDisposition"
        } else {
            null
        }
    }

    protected fun perform(requestBuilder: RequestBuilder): ResultActions {
        return mockMvc.perform(requestBuilder
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(contentStatusIsOk())
    }

    protected fun download(requestBuilder: RequestBuilder, fileName: String) {
        download(mockMvc.perform(requestBuilder), "build/$fileName")
    }

    protected fun download(requestBuilder: RequestBuilder) {
        download(mockMvc.perform(requestBuilder), null)
    }

    protected fun download(perform: ResultActions, fileName: String?) {
        var name = fileName
        val result = perform.andExpect(MockMvcResultMatchers.status().isOk()).andReturn()
        val contentType = result.response.contentType
        if (contentType != null && MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.valueOf(contentType))) {
            return
        }
        if (name == null) {
            name = getFileName(result)
        }
        if (name == null) return
        StreamUtils.copy(result.response.contentAsByteArray,
                Files.newOutputStream(Paths.get(name)))
        try {
            val filePath = System.getProperty("user.dir") + File.separator + name
            if (System.getProperties().getProperty("os.name").lowercase(Locale.getDefault()).startsWith("win")) {
                Runtime.getRuntime()
                        .exec(arrayOf("rundll32", "url.dll,FileProtocolHandler", filePath))
            } else {
                Runtime.getRuntime().exec(arrayOf("xdg-open", filePath))
            }
        } catch (ignored: Exception) {
        }
    }

    protected fun file(name: String, classPath: String): MockMultipartFile {
        val classPathResource = ClassPathResource(classPath)
        return MockMultipartFile(name, classPathResource.filename, null,
                classPathResource.inputStream)
    }


    @JvmOverloads
    protected fun json(`object`: Any?, serializationView: Class<*>? = null, incl: JsonInclude.Include? = JsonInclude.Include.NON_NULL, writeDatesAsTimestamps: Boolean = true): String {
        val objectMapper = objectMapper.setSerializationInclusion(incl)
        if (writeDatesAsTimestamps) {
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            objectMapper.registerModule(StringUtil.timeModule)
        } else {
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
        return if (serializationView != null) {
            objectMapper.writerWithView(serializationView).writeValueAsString(`object`)
        } else
            objectMapper.writeValueAsString(`object`)
    }

    protected fun requires(vararg require: String) {
        requiredParameters(*require)
    }

    protected fun tableNames(vararg tableName: String) {
        Autodoc.tableNames(*tableName)
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun logAfterAll() {
            (LoggerFactory.getILoggerFactory() as LoggerContext).getLogger(
                    "org.hibernate.SQL").level = Level.OFF
        }
    }
}
