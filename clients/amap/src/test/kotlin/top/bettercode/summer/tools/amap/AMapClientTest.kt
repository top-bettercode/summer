package top.bettercode.summer.tools.amap

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest

/**
 *
 * @author Peter Wu
 */
@Disabled
internal class AMapClientTest : BaseTest() {

    @Autowired
    lateinit var amapClient: AMapClient

    @Test
    fun regeo() {
        amapClient.regeo("104.1762947,30.8170838")
    }

    @Test
    fun geo() {
        amapClient.geo("河南省洛阳市xxxx")
    }

    @Test
    fun distance() {
        amapClient.distance("113.571864,30.928592", "112.429277,33.489804")
    }


}