package top.bettercode.summer.tools.weixin.test.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource
import top.bettercode.summer.test.BaseWebNoAuthTest
import top.bettercode.summer.tools.weixin.support.miniprogram.MiniprogramClient
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.JsSession
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfo
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfoResp
import top.bettercode.summer.tools.weixin.test.TestApplication

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
@TestPropertySource(properties = ["summer.wechat.mini.appId=XXXXXXXXXXXXXXXXXX", "summer.wechat.mini.secret=xxx"])
class MiniprogramCallbackControllerTest : BaseWebNoAuthTest() {


    @MockBean
    lateinit var miniprogramClient: MiniprogramClient

    @DisplayName("小程序手机号授权接口")
    @Test
    fun miniPhoneOauth() {
        val code = "xxx"
        val mobile = "13400000000"
        val infoResp = PhoneInfoResp(PhoneInfo(mobile, mobile, "86", null))
        Mockito.`when`(miniprogramClient.getuserphonenumber(code)).thenReturn(infoResp)

        perform(post("/wechat/miniPhoneOauth")
                .param("code", code))
    }


    @Test
    fun miniOauth() {
        val code = "xxx"
        val jsSession = JsSession(openid = "xxx")
        Mockito.`when`(miniprogramClient.jscode2session(code)).thenReturn(jsSession)
        perform(
                post("/wechat/miniOauth")
                        .param("code", code)
        )
    }

}