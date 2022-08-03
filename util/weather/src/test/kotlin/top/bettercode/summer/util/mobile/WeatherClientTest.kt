package top.bettercode.summer.util.mobile

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.util.test.BaseTest
import top.bettercode.summer.util.weather.WeatherClient

/**
 *
 * @author Peter Wu
 */
class WeatherClientTest : BaseTest() {

    @Autowired
    lateinit var weatherClient: WeatherClient

    @Test
    fun query() {
        weatherClient.query("182.148.121.104")
    }
}