package top.bettercode.summer.test.autodoc

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import top.bettercode.summer.test.BaseWebNoAuthTest


/**
 * 控制层测试
 *
 * @author Peter Wu
 */
class AutodocMockMvcControllerTest : BaseWebNoAuthTest() {

    @BeforeEach
    fun setUp() {
        Autodoc.tableNames("OAUTH_CLIENT_TOKEN")
    }

    @DisplayName("列表1")
    @Test
    fun test0Index() {
        Autodoc.requiredHeaders("sign")
        mockMvc.perform(
                get("/clientTokens")
                        .param("page", "1")
                        .param("size", "5")
        ).andExpect(status().isOk)
    }

    @Test
    fun token() {
        mockMvc.perform(
                post("/oauth/token")
                        .param("page", "1")
                        .param("size", "5")
        ).andExpect(status().isOk)
    }

    @Test
    fun test1Show() {
        mockMvc.perform(get("/clientTokens/1")).andExpect(status().isOk)
    }

    @Test
    fun test2Create() {
        mockMvc.perform(
                post("/clientTokens").contentType(MediaType.APPLICATION_JSON)
                        .content("[{}]")
//                .param("map", "{\"a\":1}")
//                .param("list", "[{\"a\":1}]")
//                .param("tokenId", "test")
//                .param("token", "1")
//                .param("authenticationId", "1")
//                .param("userName", "test")
//                .param("clientId", "1")
        ).andExpect(status().isOk)
    }

    @Test
    fun test3Update() {
        mockMvc.perform(
                put("/clientTokens/1")
                        .param("tokenId", "test")
                        .param("token", "1")
                        .param("userName", "test")
                        .param("clientId", "1")
        ).andExpect(status().isOk)
    }

    @Test
    fun test4Delete() {
        mockMvc.perform(delete("/clientTokens/1")).andExpect(status().isOk)
    }

    //测试upload
    @Test
    fun test5Upload() {
        mockMvc.perform(
                multipart("/clientTokens/upload")
                        .file(file("file", "test.png"))
        ).andExpect(status().isOk)
    }
}
