package top.bettercode.summer.tools.amap

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 *
 * @author Peter Wu
 */
object AMapUtil {

    const val X_PI: Double = Math.PI * 3000.0 / 180.0

    /**
     * gcj02转bd09
     */
    fun gcj02ToBd09(lon: Double, lat: Double): Pair<Double, Double> {
        val z = sqrt(lon * lon + lat * lat) + 0.00002 * sin(lat * X_PI)
        val theta = atan2(lat, lon) + 0.000003 * cos(lon * X_PI)
        val bdLon = z * cos(theta) + 0.0065
        val bdLat = z * sin(theta) + 0.006
        return bdLon to bdLat
    }
}