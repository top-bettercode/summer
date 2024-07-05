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
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): Double {
        require(scale >= 0) { "小数位数不能为负数" }
        return BigDecimal(this).setScale(scale, roundingMode).stripTrailingZeros().toDouble()
    }

    /**
     * 是否没有小数位数
     */
    @JvmStatic
    val Double.isInt: Boolean
        get() = this.toBigDecimal().compareTo(this.toLong().toBigDecimal()) == 0

}