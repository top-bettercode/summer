package top.bettercode.summer.test.web.support.division

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import top.bettercode.summer.test.BaseWebNoAuthTest
import top.bettercode.summer.test.TestApplication

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
class DivisionDataControllerTest : BaseWebNoAuthTest() {

    @Test
    fun list() {
        perform(
            get("/divisions/list")
                .queryParam("vnode", "true")
        )
    }

    @Test
    fun select() {
        perform(
            get("/divisions/select")
                .queryParam("code", "110000")
                .queryParam("vnode", "false")
        )
    }
}
