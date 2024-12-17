package top.bettercode.summer.tools.optimal

import java.math.BigDecimal
import java.math.RoundingMode

/**
 *
 * @author Peter Wu
 */
object OptimalUtil {
    /**
     * 默认实现<,>约束，误差
     */
    @JvmStatic
    var DEFAULT_EPSILON = 1e-3

    /**
     * 将数字转换为指定小数位数的数字
     *
     * @param scale 小数位数
     * @return 指定小数位数的数字
     */
    @JvmStatic
    @JvmOverloads
    fun Double.scale(
        scale: Int,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
    ): Double {
        require(scale >= 0) { "小数位数不能为负数" }
        return BigDecimal(this).setScale(10, roundingMode).setScale(scale, roundingMode)
            .stripTrailingZeros().toDouble()
    }

    /**
     * 容差
     */
    @JvmStatic
    fun Double.inTolerance(minEpsilon: Double): Boolean {
        val value = this.scale(10)
        return if (minEpsilon == 0.0) {
            value == 0.0
        } else {
            value > -minEpsilon && value < minEpsilon
        }
    }

    /**
     * 在范围内
     */
    @JvmStatic
    fun Double.inRange(min: Double, max: Double, minEpsilon: Double): Boolean {
        val value = this.scale(10)
        return if (minEpsilon == 0.0) {
            value in min.scale(10)..max.scale(10)
        } else {
            value > min - minEpsilon && value < max + minEpsilon
        }
    }


    @JvmStatic
    fun Double.toPlainString(): String {
        return this.toBigDecimal().setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
            .toPlainString()
    }


    /**
     * 是否没有小数位数
     */
    @JvmStatic
    val Double.isInt: Boolean
        get() = this.toBigDecimal().compareTo(this.toLong().toBigDecimal()) == 0

    /**
     * 小数位数
     */
    @JvmStatic
    val Double.scale: Int
        get() = this.toBigDecimal().scale()

}