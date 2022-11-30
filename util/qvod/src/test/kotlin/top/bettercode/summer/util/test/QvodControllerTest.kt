package top.bettercode.summer.util.test

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/**
 * @author Peter Wu
 */
@SpringBootTest
class QvodControllerTest : BaseWebNoAuthTest() {

    @Test
    fun signature() {
        perform(
            get("/qvod/signature")
        )
    }

    @Test
    fun antiLeechUrl() {
        perform(
            post("/qvod/antiLeechUrl").param(
                "url",
                "https://1313291945.vod2.myqcloud.com/3306e890vodtranscq1313291945/7a0382da387702307226359579/v.f80000.mp4"
            )
        )
    }

}