package top.bettercode.summer.tools.weixin.test.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.TestPropertySource
import top.bettercode.summer.test.BaseWebNoAuthTest
import top.bettercode.summer.tools.weixin.support.miniprogram.MiniprogramClient
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfo
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.PhoneInfoResp
import top.bettercode.summer.tools.weixin.test.TestApplication

/**
 * @author Peter Wu
 */
//@Disabled
@SpringBootTest(classes = [TestApplication::class])
@TestPropertySource(properties = ["summer.wechat.mini.appId=XXXXXXXXXXXXXXXXXX", "summer.wechat.mini.secret=xxx"])
class MiniCallbackControllerTest : BaseWebNoAuthTest() {


    @MockBean
    lateinit var miniprogramClient: MiniprogramClient

    @DisplayName("小程序手机号授权接口")
    @Test
    fun miniPhoneOauth() {
        val code = "xxx"
        val mobile = "13400000000"
        val infoResp = PhoneInfoResp(PhoneInfo(mobile, mobile, "86", null))
        Mockito.`when`(miniprogramClient.getuserphonenumber(code,1)).thenReturn(infoResp)

        perform(post("/wechat/miniPhoneOauth")
                .param("code", code))
    }


    @Test
    fun miniOauth() {
        perform(
                post("/wechat/miniOauth")
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