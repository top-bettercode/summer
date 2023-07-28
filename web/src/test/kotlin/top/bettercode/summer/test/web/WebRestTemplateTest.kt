package top.bettercode.summer.test.web

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebRestTemplateTest {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var environment: Environment

    @Value("\${logging.level.org.springframework.boot.SpringApplication}")
    var logLevel: String? = null

    @Test
    fun env() {
        System.err.println(logLevel)
    }

    @Test
    fun test() {
        val entity = restTemplate
                .getForEntity("/webtest?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1",
                        String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun optionserror() {
        val httpMethods = restTemplate.optionsForAllow("/errors")
        println(httpMethods)
    }

    @Test
    fun error() {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.ALL)
        val entity = restTemplate
                .postForEntity("/weberrors", HttpEntity<Any>(headers), String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }
}