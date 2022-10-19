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
    @Test
    fun antiLeechUrl() {
        perform(
            MockMvcRequestBuilders.post("/qvod/antiLeechUrl").param("url","https://1313291945.vod2.myqcloud.com/3306e890vodtranscq1313291945/7a0382da387702307226359579/v.f80000.mp4")
        )
    }

}