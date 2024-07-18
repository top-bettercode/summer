package top.bettercode.summer.logging

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [(TestController::class)],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@TestPropertySource(properties = ["summer.logging.slack.channel=dev"])
class LoggingTemplateControllerTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate
    private val requestBody = "///////////////////////request_body///////////////////////"

    @Test
    fun apiAddress() {
        System.err.println(LoggingUtil.apiAddress)
    }

    @Test
    fun test() {
        val entity = testRestTemplate.postForEntity("/test", requestBody, String::class.java)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        org.junit.jupiter.api.Assertions.assertEquals(requestBody, entity.body)
    }

    @Test
    fun testNoRead() {
        val entity = testRestTemplate.postForEntity("/testNoRead", requestBody, String::class.java)
//        Thread.sleep(20 * 1000L)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        org.junit.jupiter.api.Assertions.assertEquals("null", entity.body)
    }

    @Test
    fun testNoReqestbody() {
        val entity = testRestTemplate.postForEntity("/testNoRead", null, String::class.java)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        org.junit.jupiter.api.Assertions.assertEquals("null", entity.body)
    }

    @Disabled
    @Test
    fun error() {
        val entity = testRestTemplate.postForEntity("/error/1", null, String::class.java)
        Thread.sleep(10 * 1000L)
        org.junit.jupiter.api.Assertions.assertEquals(
            HttpStatus.OK,
            entity.statusCode
        )
    }

    @Test
    fun encrypted() {
        val params = LinkedMultiValueMap<String, String>()
        params.add("password", "adbcef")
        val headers = HttpHeaders()
        headers.add("token", "adbcef")
        val entity = testRestTemplate.postForEntity(
            "/encrypted2",
            HttpEntity(params, headers),
            String::class.java
        )
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun multipart() {
        val params = LinkedMultiValueMap<String, Any>()
        params.add("password", "adbcef")
        params.add("user", "adbcef")
        params.add("file", ClassPathResource("application.yml"))
        val headers = HttpHeaders()
        headers.add("token", "adbcef")
        val entity = testRestTemplate.postForEntity(
            "/multipart",
            HttpEntity(params, headers),
            String::class.java
        )
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

}
