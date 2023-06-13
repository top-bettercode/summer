package top.bettercode.summer.test.web.support.gb2260

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import top.bettercode.summer.test.BaseWebNoAuthTest
import top.bettercode.summer.test.TestApplication
import top.bettercode.summer.web.*

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
class GB2260ControllerTest : BaseWebNoAuthTest() {
    @Test
    fun list() {
        perform(get("/divisions/list")
                .param("vnode", "false")
        )
    }

    @Test
    fun select() {
        perform(
                get("/divisions/select")
                        .param("code", "110000")
                        .param("vnode", "false")
        )
    }
}
