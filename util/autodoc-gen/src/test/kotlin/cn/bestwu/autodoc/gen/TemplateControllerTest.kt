package cn.bestwu.autodoc.gen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.FileReader
import javax.sql.DataSource

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["api.sign.handler-type-prefix=", "logging.level.root=debug"])
class TemplateControllerTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var dataSource: DataSource

    @BeforeEach
    fun setUp() {
        Autodoc.tableNames("OAUTH_CLIENT_TOKEN")
        NoCommitScriptRunner(dataSource.connection).runScript(FileReader(ClassPathResource("import.sql").file))
    }

    @Test
    fun test() {
        val entity = restTemplate.getForEntity("/clientTokens?page=1&size=5", String::class.java)
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
    }
}