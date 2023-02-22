package top.bettercode.summer.web.support.gb2260

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import top.bettercode.summer.test.BaseWebNoAuthTest
import top.bettercode.summer.web.TestApplication

/**
 *
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
class GB2260ControllerTest : BaseWebNoAuthTest() {

    @Test
    fun list() {
        perform(get("/divisions/list"))
    }

    @Test
    fun select() {
        perform(
            get("/divisions/select")
                .param("code", "")
        )
    }
}