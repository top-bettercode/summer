package top.bettercode.summer.tools.weixin.test.controller

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import top.bettercode.summer.test.BaseWebNoAuthTest
import top.bettercode.summer.tools.weixin.test.TestApplication

/**
 * @author Peter Wu
 */
@Disabled
@SpringBootTest(classes = [TestApplication::class])
class CallbackControllerTest : BaseWebNoAuthTest() {

    @Test
    fun miniOauth() {
        perform(
            post("/wechat/miniOauth")
//                .param("code", "xxx")
        )
    }

    @Test
    fun miniPhoneOauth() {
        perform(
            post("/wechat/miniPhoneOauth")
//                .param("code", "xxx")
        )
    }


    @Test
    fun jsSignUrl() {
        perform(
            get("/wechat/jsSign")
                .param("url", "https://xxx.com")
        )
    }
}