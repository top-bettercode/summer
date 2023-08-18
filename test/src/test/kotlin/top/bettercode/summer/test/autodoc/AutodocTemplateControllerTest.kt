package top.bettercode.summer.test.autodoc

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = [
    "summer.web.wrap-enable=false",
    "summer.web.ok-enable=false",
    "summer.sign.handler-type-prefix=",
    "logging.level.root=debug"]
)
class AutodocTemplateControllerTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @BeforeEach
    fun setUp() {
        Autodoc.tableNames("OAUTH_CLIENT_TOKEN")
    }

    @Test
    fun test() {
        val entity = restTemplate.getForEntity("/clientTokens?page=1&size=5", String::class.java)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }
}