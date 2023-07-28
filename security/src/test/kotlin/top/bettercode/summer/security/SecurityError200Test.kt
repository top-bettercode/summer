package top.bettercode.summer.security

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import top.bettercode.summer.security.config.ApiSecurityProperties
import java.util.*

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class SecurityError200Test {
    @Autowired
    var apiSecurityProperties: ApiSecurityProperties? = null

    @Autowired
    lateinit var restTemplate: TestRestTemplate
    val username = "root"

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun unAuth() {
        val httpHeaders = HttpHeaders()
        val entity = restTemplate
                .exchange("/testDefaultAuth", HttpMethod.POST,
                        HttpEntity(Collections.singletonMap("aa", "xxx"), httpHeaders),
                        String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun authInParam() {
        val httpHeaders = HttpHeaders()
        val entity = restTemplate
                .exchange("/testDefaultAuth?access_token=", HttpMethod.GET,
                        HttpEntity<Any>(httpHeaders), String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun noToken() {
        val httpHeaders = HttpHeaders()
        //    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        val entity = restTemplate
                .exchange("/testDefaultAuth", HttpMethod.GET, HttpEntity<Any>(httpHeaders),
                        String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun invalidToken() {
        val httpHeaders = HttpHeaders()
        val entity = restTemplate
                .exchange("/testDefaultAuth?access_token=xxx", HttpMethod.GET,
                        HttpEntity<Any>(httpHeaders),
                        String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun testNoAuthInvalidToken() {
        val httpHeaders = HttpHeaders()
        val entity = restTemplate
                .exchange("/testNoAuth?access_token=xxx", HttpMethod.GET, HttpEntity<Any>(httpHeaders),
                        String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

//    @Test
    fun expiredToken() {
        val httpHeaders = HttpHeaders()
        val entity = restTemplate
                .exchange(
                        "/testDefaultAuth?access_token=",
                        HttpMethod.GET, HttpEntity<Any>(httpHeaders),
                        String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun accessTokenError() {
        val params: MultiValueMap<String, Any> = LinkedMultiValueMap()
        params.add("grant_type", "password")
        params.add("scope", "trust")
        params.add("useDefaultAuthorization", "true")
        params.add("username", username)
        params.add("password", "wrong password")
        params.add("openId", "")
        val entity = restTemplate
                .postForEntity("/oauth/token", HttpEntity(params),
                        String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }
}
