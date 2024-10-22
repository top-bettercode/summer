package top.bettercode.summer.tools.amap

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class AMapUtilTest {

    @Test
    fun gcj02ToBd09() {
        //114.21892734521,29.575429778924
        //      "x": 114.22539195429346,
        //      "y": 29.58158536745758
        val (lng, lat) = AMapUtil.gcj02ToBd09(114.21892734521, 29.575429778924)
        System.err.println("$lng:$lat")
        assertEquals(114.22539195429346, lng, 1e-10)
        assertEquals(29.58158536745758, lat, 1e-10)
    }
}