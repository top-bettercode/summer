package top.bettercode.summer.web.support

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * @author Peter Wu
 */
object KilogramUtil {
    /**
     * @param kilogram 单位千克
     * @return 单位克
     */
    @JvmStatic
    fun toGram(kilogram: String?): Long {
        return BigDecimal(kilogram).setScale(3, RoundingMode.HALF_UP)
                .multiply(BigDecimal(1000)).toLong()
    }

    /**
     * @param gram 单位克
     * @return 单位千克
     */
    fun toKilogram(gram: Long): BigDecimal {
        return toKilogram(BigDecimal(gram), 3)
    }

    /**
     * @param gram  单位克
     * @param scale 小数位数
     * @return 单位千克
     */
    @JvmStatic
    fun toKilogram(gram: BigDecimal, scale: Int): BigDecimal {
        return gram.divide(BigDecimal(1000), scale, RoundingMode.HALF_UP)
    }
}
