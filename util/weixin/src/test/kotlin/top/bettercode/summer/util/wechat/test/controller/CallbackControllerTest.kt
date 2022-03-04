package top.bettercode.summer.util.wechat.test.controller

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import top.bettercode.summer.util.test.BaseWebNoAuthTest
import top.bettercode.summer.util.wechat.test.TestApplication

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
class CallbackControllerTest : BaseWebNoAuthTest() {

    @Test
    fun miniOauth() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/wechat/miniOauth")
//                .param("code", "xxx")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun jsSignUrl() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/wechat/jsSign")
                .param("url", "https://xxx.com")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}