package top.bettercode.api.sign

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import top.bettercode.summer.apisign.ApiSignAlgorithm
import top.bettercode.summer.apisign.ApiSignConfiguration

/**
 * @author Peter Wu
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiSignConfiguration::class, SignTestController::class], properties = [
        "summer.web.wrap-enable=false",
        "summer.web.ok-enable=false",
        "summer.auto-sign.enabled=false",
        "summer.sign.clientSecret=abcd",
        "summer.sign.handler-type-prefix=top.bettercode.api.sign.SignTestController",
        "logging.level.root=debug"
    ], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class SignTestControllerTest {

    @Autowired
    lateinit var api: ApiSignAlgorithm

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun testGetSuccess() {
        val entity = testRestTemplate.getForEntity(
            "/testSign?limit=25&page=0&size=25&start=0&type=0&sign=" + api.sign(
                LinkedMultiValueMap(
                    mutableMapOf<String, List<String>>(
                        "limit" to listOf("25"),
                        "page" to listOf("0"),
                        "size" to listOf("25"),
                        "start" to listOf("0"),
                        "type" to listOf("0")
                    )
                )
            ), String::class.java
        )
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        Assertions.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }

    @Test
    fun testGetFail() {
        val entity = testRestTemplate.getForEntity(
            "/testSign?limit=25&page=0&size=25&start=0&type=0&sign=",
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, entity.statusCode)
        System.err.println(entity.body)
    }

    @Test
    fun testPostSuccess() {
        val requestParams = LinkedMultiValueMap(
            mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0"),
                "todoUrl" to listOf("/#/partyMember/edit-applyParty?type=auditing&formId=487")
            )
        )
        requestParams["sign"] = listOf(api.sign(requestParams))
        val entity = testRestTemplate.postForEntity("/testSign", requestParams, String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        Assertions.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }

    @Test
    fun testPostFail() {
        val requestParams = LinkedMultiValueMap(
            mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0")
            )
        )
        val entity = testRestTemplate.postForEntity("/testSign", requestParams, String::class.java)
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, entity.statusCode)
        System.err.println(entity.body)
    }


    @Test
    fun testEmptyParamsSuccess() {
        val requestParams = LinkedMultiValueMap(
            mutableMapOf<String, List<String>>(
            )
        )
        val entity = testRestTemplate.postForEntity("/testSign", requestParams, String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        Assertions.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }

    @Test
    fun testSignIgnoreTypeSuccess() {
        val requestParams = LinkedMultiValueMap(
            mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0")
            )
        )
        val entity =
            testRestTemplate.postForEntity("/apiSignIgnore/type", requestParams, String::class.java)
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        Assertions.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }

    @Test
    fun testSignIgnoreMthodSuccess() {
        val requestParams = LinkedMultiValueMap(
            mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0")
            )
        )
        val entity = testRestTemplate.postForEntity(
            "/apiSignIgnore/method",
            requestParams,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        Assertions.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }
}
