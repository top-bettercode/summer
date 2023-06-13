package top.bettercode.summer.test.web.config

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.test.BaseWebNoAuthTest

/**
 * @author Peter Wu
 */
internal class WebMvcConfigurationTest : BaseWebNoAuthTest() {
    @Disabled
    @Test
    fun test() {
        download(get("/captcha.jpg"), "captcha.jpg")
    }
}