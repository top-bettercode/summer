package top.bettercode.summer.tools.weixin.test.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import top.bettercode.summer.test.BaseWebNoAuthTest
import top.bettercode.summer.tools.weixin.support.corp.CorpClient
import top.bettercode.summer.tools.weixin.support.corp.entity.CorpWebPageAccessToken
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.WebPageAccessToken
import top.bettercode.summer.tools.weixin.test.TestApplication

/**
 *
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
@TestPropertySource(properties = ["summer.wechat.corp.appId=XXXXXXXXXXXXXXXXXX", "summer.wechat.corp.secret=xxx", "summer.wechat.corp.wechat-base-url=https://xxx.com", "summer.wechat.corp.wechat-web-oauth-url=/oauth"])
class CorpCallbackControllerTest : BaseWebNoAuthTest() {


    @SpyBean
    lateinit var corpClient: CorpClient

    @Test
    fun oauth() {
        val code = "xxx"
        val accessToken = CorpWebPageAccessToken(openid = "xxx")
        Mockito.doReturn(accessToken).`when`(corpClient).getWebPageAccessToken(code)
        mockMvc.perform(get("/wechat/corpOauth")
                .param("code", code)
                .param("state", code)
        ).andExpect(MockMvcResultMatchers.status().is3xxRedirection())

    }

}