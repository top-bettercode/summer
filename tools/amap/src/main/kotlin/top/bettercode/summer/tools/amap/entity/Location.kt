package top.bettercode.summer.tools.amap.entity

import top.bettercode.summer.tools.amap.AMapClient
import java.math.BigDecimal
import java.math.RoundingMode
import javax.validation.constraints.NotBlank

/**
 *
 * @author Peter Wu
 */
class Location(lng: String = "", lat: String = "") {

    @NotBlank
    var lng: String = lng
        get() = BigDecimal(field).setScale(
            6,
            RoundingMode.HALF_UP
        ).toPlainString()

    @NotBlank
    var lat: String = lat
        get() = BigDecimal(field).setScale(6, RoundingMode.HALF_UP).toPlainString()

    override fun toString(): String {
        return "${lng},${lat}"
    }
}
