package top.bettercode.summer.tools.amap

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.amap.entity.Extensions

/**
 *
 * @author Peter Wu
 */
//@Disabled
internal class AMapClientTest : BaseTest() {

    @Autowired
    lateinit var amapClient: AMapClient

    @Test
    fun regeo() {
        val regeo = amapClient.regeo(
            location = "116.481488,39.990464",
            extensions = Extensions.ALL
        )
        System.err.println(regeo.regeocode?.formattedAddress)
    }

    @Test
    fun geo() {
        val geo = amapClient.geo("河南省洛阳市xxxx")
        if (geo.hasGeocodes) {
            System.err.println(geo.geocodes?.first()?.location)
        }
    }

    @Test
    fun distance() {
        val distance = amapClient.distance("113.571864,30.928592", "112.429277,33.489804")
        System.err.println(distance.results?.first()?.distance)
    }


}