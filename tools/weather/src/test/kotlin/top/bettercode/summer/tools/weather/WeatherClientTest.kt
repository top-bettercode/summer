package top.bettercode.summer.tools.weather

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest

/**
 *
 * @author Peter Wu
 */
class WeatherClientTest : BaseTest() {

    @Autowired
    lateinit var weatherClient: WeatherClient

    @Test
    fun query1() {
        weatherClient.query("182.148.121.104")
    }

    @Test
    fun query2() {
        weatherClient.query(104.176376, 30.817039)
    }

}