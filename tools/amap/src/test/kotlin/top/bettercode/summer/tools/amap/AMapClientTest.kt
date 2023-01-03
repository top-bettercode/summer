package top.bettercode.summer.tools.amap

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.amap.entity.Location

/**
 *
 * @author Peter Wu
 */
internal class AMapClientTest : BaseTest() {

    @Autowired
    lateinit var amapClient: AMapClient

    @Test
    fun regeo() {
        val regeo = amapClient.regeo(
            Location("104.1762947", "30.8170838")
        )
        System.err.println(regeo)
    }

}