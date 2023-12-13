package top.bettercode.summer.security

import com.fasterxml.jackson.databind.ObjectMapper
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
import top.bettercode.summer.security.token.AccessToken
import top.bettercode.summer.test.autodoc.Autodoc.collectionName
import top.bettercode.summer.test.autodoc.Autodoc.description
import top.bettercode.summer.test.autodoc.Autodoc.disable
import top.bettercode.summer.test.autodoc.Autodoc.enable
import top.bettercode.summer.test.autodoc.Autodoc.name
import top.bettercode.summer.test.autodoc.Autodoc.requiredHeaders
import top.bettercode.summer.test.autodoc.Autodoc.requiredParameters
import java.util.*

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], properties = ["summer.web.wrap-enable=false"], webEnvironment = WebEnvironment.RANDOM_PORT)
class Security200Test {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var clientRestTemplate: TestRestTemplate

    @Autowired
    var apiSecurityProperties: ApiSecurityProperties? = null
    val objectMapper = ObjectMapper()
    val username = "root"
    val password: String? = DigestUtils.md5DigestAsHex("123456".toByteArray())

    @BeforeEach
    fun setUp() {
        collectionName = "登录授权"
        requiredHeaders(HttpHeaders.AUTHORIZATION)
        clientRestTemplate = restTemplate.withBasicAuth(apiSecurityProperties!!.clientId,
                apiSecurityProperties!!.clientSecret)
    }

    private fun getApiAccessToken(tag: String?): AccessToken {
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
        return objectMapper.readValue(body, AccessToken::class.java)
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
        Assertions.assertEquals(HttpStatus.OK, entity2.statusCode)
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
        Assertions.assertEquals(HttpStatus.OK, entity2.statusCode)
//        Thread.sleep(1000)
    }

    @Test
    fun auth() {
        val httpHeaders = HttpHeaders()
        httpHeaders[HttpHeaders.AUTHORIZATION] = "bearer " + getApiAccessToken("auth").accessToken
        val entity = restTemplate
                .exchange("/testDefaultAuth", HttpMethod.POST,
                        HttpEntity(Collections.singletonMap("aa", "xxx"), httpHeaders), String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }
}
