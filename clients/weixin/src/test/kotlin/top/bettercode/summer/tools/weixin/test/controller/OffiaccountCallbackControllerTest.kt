package top.bettercode.summer.tools.weixin.test.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import top.bettercode.summer.test.BaseWebNoAuthTest
import top.bettercode.summer.tools.weixin.support.offiaccount.OffiaccountClient
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.WebPageAccessToken
import top.bettercode.summer.tools.weixin.test.TestApplication

/**
 *
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
@TestPropertySource(properties = ["summer.wechat.appId=XXXXXXXXXXXXXXXXXX", "summer.wechat.secret=xxx", "summer.wechat.wechat-base-url=https://xxx.com", "summer.wechat.wechat-web-oauth-url=/oauth"])
class OffiaccountCallbackControllerTest : BaseWebNoAuthTest() {

    @SpyBean
    lateinit var offiaccountClient: OffiaccountClient

    @Test
    fun oauth() {
        val code = "xxx"
        val accessToken = WebPageAccessToken(openid = "xxx")
        Mockito.doReturn(accessToken).`when`(offiaccountClient).getWebPageAccessToken(code)
        mockMvc.perform(get("/wechat/oauth")
                .param("code", code)
                .param("state", code)
        ).andExpect(MockMvcResultMatchers.status().is3xxRedirection())

    }

    @Test
    fun jsSignUrl() {
        Mockito.doReturn("xxx").`when`(offiaccountClient).getJsapiTicket()
        perform(
                get("/wechat/jsSign")
                        .param("url", "https://xxx.com")
        )
    }
}