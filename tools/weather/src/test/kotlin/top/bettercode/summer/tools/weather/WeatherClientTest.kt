package top.bettercode.summer.tools.weather

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest

/**
 *
 * @author Peter Wu
 */
//@Disabled
class WeatherClientTest : BaseTest() {

    @Autowired
    lateinit var weatherClient: WeatherClient

    @Test
    fun type() {
        val typeList = weatherClient.type()
        System.err.println(typeList)
    }

    @Test
    fun query1() {
        val result = weatherClient.query("182.148.121.104")
        System.err.println(result)
    }

    @Test
    fun query2() {
        val result = weatherClient.query(104.176376, 30.817039)
        System.err.println(result)
    }

}