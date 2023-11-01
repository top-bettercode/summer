package top.bettercode.summer.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
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
import org.springframework.util.DigestUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import top.bettercode.summer.security.config.ApiSecurityProperties
import top.bettercode.summer.security.token.ApiAccessToken
import top.bettercode.summer.test.autodoc.Autodoc.collectionName
import top.bettercode.summer.test.autodoc.Autodoc.description
import top.bettercode.summer.test.autodoc.Autodoc.disable
import top.bettercode.summer.test.autodoc.Autodoc.enable
import top.bettercode.summer.test.autodoc.Autodoc.name
import top.bettercode.summer.test.autodoc.Autodoc.requiredHeaders
import top.bettercode.summer.test.autodoc.Autodoc.requiredParameters
import top.bettercode.summer.web.RespEntity

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], properties = ["summer.web.ok-enable=false"], webEnvironment = WebEnvironment.RANDOM_PORT)
class SecurityTest {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var clientRestTemplate: TestRestTemplate

    @Autowired
    var apiSecurityProperties: ApiSecurityProperties? = null
    val objectMapper = ObjectMapper()
    var username = "root"
    val password: String? = DigestUtils.md5DigestAsHex("123456".toByteArray())

    @BeforeEach
    fun setUp() {
        collectionName = "登录授权"
        requiredHeaders(HttpHeaders.AUTHORIZATION)
        clientRestTemplate = restTemplate.withBasicAuth(apiSecurityProperties!!.clientId,
                apiSecurityProperties!!.clientSecret)
    }

    private fun getApiAccessToken(tag: String?): ApiAccessToken {
        val params: MultiValueMap<String, Any> = LinkedMultiValueMap()
        params.add("grant_type", "password")
        params.add("scope", "app")
        params.add("username", username)
        params.add("password", password)
        val headers = HttpHeaders()
        if (!tag.isNullOrBlank()) {
            headers.add("tag", tag)
        }
        val entity = clientRestTemplate
                .postForEntity("/oauth/token", HttpEntity(params, headers), String::class.java)
        val body = entity.body
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        val resp = objectMapper
                .readValue<RespEntity<ApiAccessToken>>(body, TypeFactory.defaultInstance().constructParametricType(
                        RespEntity::class.java, ApiAccessToken::class.java))
        return resp.data!!
    }

    @Test
    fun accessToken() {
        description = ""
        name = "获取accessToken"
        requiredParameters("grant_type", "scope", "username", "password")
        val accessToken = getApiAccessToken(null)
        Assertions.assertNotNull(accessToken)
//        Thread.sleep(1000)
    }

    /**
     * 刷新token
     */
    @Test
    fun refreshToken() {
        disable()
        val params: MultiValueMap<String, Any> = LinkedMultiValueMap()
        params.add("grant_type", "refresh_token")
        params.add("scope", "app")
        params.add("refresh_token", getApiAccessToken("refresh_token").refreshToken)
        enable()
        name = "刷新accessToken"
        requiredParameters("grant_type", "scope", "refresh_token")
        val entity2 = clientRestTemplate
                .postForEntity("/oauth/token", HttpEntity(params), String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity2.statusCode)
//        Thread.sleep(1000)
    }

    @Test
    fun revokeToken() {
        disable()
        val accessToken = getApiAccessToken("revokeToken").accessToken
        enable()
        name = "撤销accessToken"
        val httpHeaders = HttpHeaders()
        httpHeaders[HttpHeaders.AUTHORIZATION] = "bearer $accessToken"
        val entity2 = restTemplate.exchange("/oauth/token",
                HttpMethod.DELETE, HttpEntity<Any>(httpHeaders),
                String::class.java)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, entity2.statusCode)
    }

    @Test
    fun revokeToken2() {
        disable()
        val params: MultiValueMap<String, Any> = LinkedMultiValueMap()
        params.add("grant_type", "revoke_token")
        params.add("revoke_token", getApiAccessToken("revokeToken2").accessToken)
        enable()
        name = "撤销accessToken"
        requiredParameters("grant_type",  "revokeToken")
        val entity2 = clientRestTemplate
                .postForEntity("/oauth/token", HttpEntity(params), String::class.java)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, entity2.statusCode)
//        Thread.sleep(1000)
    }

    @Test
    fun auth() {
        val httpHeaders = HttpHeaders()
        httpHeaders[HttpHeaders.AUTHORIZATION] = "bearer " + getApiAccessToken("auth").accessToken
        val entity = restTemplate
                .exchange("/testDefaultAuth", HttpMethod.POST, HttpEntity<Any>(httpHeaders), String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun authInParam() {
        val httpHeaders = HttpHeaders()
        val entity = restTemplate
                .exchange("/testDefaultAuth?access_token=" + getApiAccessToken("authInParam").accessToken,
                        HttpMethod.GET,
                        HttpEntity<Any>(httpHeaders), String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun authority() {
        val httpHeaders = HttpHeaders()
        httpHeaders[HttpHeaders.AUTHORIZATION] = "bearer " + getApiAccessToken("authority").accessToken
        val entity = restTemplate
                .exchange("/testAuth", HttpMethod.GET, HttpEntity<Any>(httpHeaders), String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun noauthority() {
        username = "peter"
        val httpHeaders = HttpHeaders()
        httpHeaders[HttpHeaders.AUTHORIZATION] = "bearer " + getApiAccessToken("noauthority").accessToken
        val entity = restTemplate
                .exchange("/testAuth", HttpMethod.GET, HttpEntity<Any>(httpHeaders), String::class.java)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, entity.statusCode)
    }

    @Test
    fun unauthority() {
        val httpHeaders = HttpHeaders()
        val entity = restTemplate
                .exchange("/testAuth", HttpMethod.GET, HttpEntity<Any>(httpHeaders), String::class.java)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, entity.statusCode)
    }

    @Test
    fun testNoAuth() {
        val entity = restTemplate.getForEntity("/testNoAuth", String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun testNoAuthWithToken() {
        val httpHeaders = HttpHeaders()
        httpHeaders[HttpHeaders.AUTHORIZATION] = "bearer " + getApiAccessToken("testNoAuthWithToken").accessToken
        val entity = restTemplate
                .exchange("/testNoAuth", HttpMethod.GET, HttpEntity<Any>(httpHeaders), String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun testPublicSource() {
        val httpHeaders = HttpHeaders()
        //    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + getApiToken().getAccessToken());
        val entity = restTemplate
                .exchange("/test2.json", HttpMethod.GET, HttpEntity<Any>(httpHeaders), String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }

    @Test
    fun testStaticSource() {
        val httpHeaders = HttpHeaders()
        //    httpHeaders.set(HttpHeaders.AUTHORIZATION, "bearer " + getApiToken().getAccessToken());
        val entity = restTemplate
                .exchange("/test.json", HttpMethod.GET, HttpEntity<Any>(httpHeaders), String::class.java)
        System.err.println(entity.body)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }
}
