package cn.bestwu.loggingtest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [(TestController::class)], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TemplateControllerTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate
    private val requestBody = "///////////////////////request_body///////////////////////"

    @Test
    fun test() {
        val entity = testRestTemplate.postForEntity("/test", requestBody, String::class.java)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        org.junit.jupiter.api.Assertions.assertEquals(requestBody, entity.body)
    }

    @Test
    fun testNoRead() {
        val entity = testRestTemplate.postForEntity("/testNoRead", requestBody, String::class.java)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        org.junit.jupiter.api.Assertions.assertEquals("null", entity.body)
    }

    @Test
    fun testNoReqestbody() {
        val entity = testRestTemplate.postForEntity("/testNoRead", null, String::class.java)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        org.junit.jupiter.api.Assertions.assertEquals("null", entity.body)
    }

    @Test
    fun error() {
        val entity = testRestTemplate.postForEntity("/errorMail/1", null, String::class.java)
        Thread.sleep(20 * 1000)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.statusCode)
    }

    @Test
    fun encrypted() {
        val params = LinkedMultiValueMap<String, String>()
        params.add("password", "adbcef")
        val headers = HttpHeaders()
        headers.add("token", "adbcef")
        val entity = testRestTemplate.postForEntity("/encrypted2", HttpEntity(params, headers), String::class.java)
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
        val entity = testRestTemplate.postForEntity("/multipart", HttpEntity(params, headers), String::class.java)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

}
