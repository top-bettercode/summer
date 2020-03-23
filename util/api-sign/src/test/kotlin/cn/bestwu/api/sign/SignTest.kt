package cn.bestwu.api.sign

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap

/**
 * @author Peter Wu
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [ApiSignConfiguration::class, TestController::class], properties = ["api.sign.clientSecret=abcd", "api.sign.handler-type-prefix=cn.bestwu.api.sign.TestController","logging.level.root=debug"], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SignTest {

    @Autowired
    lateinit var api: ApiSignAlgorithm

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun testGetSuccess() {
        val entity = testRestTemplate.getForEntity("/test?limit=25&page=0&size=25&start=0&type=0&sign=" + api.sign(LinkedMultiValueMap<String, String>(mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0")
        ))), String::class.java)
        Assert.assertEquals(HttpStatus.OK, entity.statusCode)
        Assert.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }

    @Test
    fun testGetFail() {
        val entity = testRestTemplate.getForEntity("/test?limit=25&page=0&size=25&start=0&type=0&sign=", String::class.java)
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE, entity.statusCode)
        System.err.println(entity.body)
    }

    @Test
    fun testPostSuccess() {
        val requestParams = LinkedMultiValueMap<String, String>(mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0"),
                "todoUrl" to listOf("/#/partyMember/edit-applyParty?type=auditing&formId=487")
        ))
        requestParams["sign"] = listOf(api.sign(requestParams))
        val entity = testRestTemplate.postForEntity("/test", requestParams, String::class.java)
        Assert.assertEquals(HttpStatus.OK, entity.statusCode)
        Assert.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }

    @Test
    fun testPostFail() {
        val requestParams = LinkedMultiValueMap<String, String>(mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0")
        ))
        val entity = testRestTemplate.postForEntity("/test", requestParams, String::class.java)
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE, entity.statusCode)
        System.err.println(entity.body)
    }


    @Test
    fun testEmptyParamsSuccess() {
        val requestParams = LinkedMultiValueMap<String, String>(mutableMapOf<String, List<String>>(
        ))
        val entity = testRestTemplate.postForEntity("/test", requestParams, String::class.java)
        Assert.assertEquals(HttpStatus.OK, entity.statusCode)
        Assert.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }

    @Test
    fun testSignIgnoreTypeSuccess() {
        val requestParams = LinkedMultiValueMap<String, String>(mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0")
        ))
        val entity = testRestTemplate.postForEntity("/apiSignIgnore/type", requestParams, String::class.java)
        Assert.assertEquals(HttpStatus.OK, entity.statusCode)
        Assert.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }

    @Test
    fun testSignIgnoreMthodSuccess() {
        val requestParams = LinkedMultiValueMap<String, String>(mutableMapOf<String, List<String>>(
                "limit" to listOf("25"),
                "page" to listOf("0"),
                "size" to listOf("25"),
                "start" to listOf("0"),
                "type" to listOf("0")
        ))
        val entity = testRestTemplate.postForEntity("/apiSignIgnore/method", requestParams, String::class.java)
        Assert.assertEquals(HttpStatus.OK, entity.statusCode)
        Assert.assertEquals("success", entity.body)
        System.err.println(entity.body)
    }
}
