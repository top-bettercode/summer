package top.bettercode.summer.util.qvod

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import top.bettercode.summer.util.test.BaseWebNoAuthTest

/**
 * @author Peter Wu
 */
@SpringBootTest(classes = [TestApplication::class])
class QvodControllerTest : BaseWebNoAuthTest() {

    @Test
    fun signature() {
        perform(
            MockMvcRequestBuilders.get("/qvod/signature")
        )
    }

}