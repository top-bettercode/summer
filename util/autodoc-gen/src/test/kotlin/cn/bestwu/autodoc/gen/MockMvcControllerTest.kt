package cn.bestwu.autodoc.gen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.FileReader
import javax.sql.DataSource


/**
 * 控制层测试
 *
 * @author Peter Wu
 */
@ExtendWith(value = [SpringExtension::class, AutodocSetting::class])
@WebMvcTest(ClientTokenController::class)
@DisplayName("token")
class MockMvcControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var dataSource: DataSource

    @BeforeEach
    fun setUp() {
        NoCommitScriptRunner(dataSource.connection).runScript(FileReader(ClassPathResource("import.sql").file))
        Autodoc.tableNames("OAUTH_CLIENT_TOKEN")
    }


    @DisplayName("列表1")
    @Test
    @Throws(Exception::class)
    fun test0Index() {
        Autodoc.requiredHeaders("sign")
        mockMvc.perform(get("/clientTokens")
                .param("page", "1")
                .param("size", "5")
        ).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun token() {
        mockMvc.perform(post("/oauth/token")
                .param("page", "1")
                .param("size", "5")
        ).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun test1Show() {
        mockMvc.perform(get("/clientTokens/1")).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun test2Create() {
        mockMvc.perform(post("/clientTokens").contentType(MediaType.APPLICATION_JSON)
            .content("[{}]")
//                .param("map", "{\"a\":1}")
//                .param("list", "[{\"a\":1}]")
//                .param("tokenId", "test")
//                .param("token", "1")
//                .param("authenticationId", "1")
//                .param("userName", "test")
//                .param("clientId", "1")
        ).andExpect(status().isCreated)
    }

    @Test
    @Throws(Exception::class)
    fun test3Update() {
        mockMvc.perform(put("/clientTokens/1")
                .param("tokenId", "test")
                .param("token", "1")
                .param("userName", "test")
                .param("clientId", "1")
        ).andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun test4Delete() {
        mockMvc.perform(delete("/clientTokens/1")).andExpect(status().isNoContent)
    }

}
