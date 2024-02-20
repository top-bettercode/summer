package top.bettercode.summer.tools.optimal.solver

import kotlin.math.pow

/**
 *
 * @author Peter Wu
 */
object OptimalUtil {
    /**
     * 默认实现<,>约束，误差
     */
    @JvmStatic
    var DEFAULT_EPSILON = 1e-4

    /**
     * 求解结果小数位
     *
     * Math.abs(Math.log10(epsilon)).toInt()
     */
    @JvmStatic
    var DEFAULT_SCALE: Int = 10

    /**
     * 将数字转换为指定小数位数的数字
     *
     * @param number        数字
     * @param scale 小数位数
     * @return 指定小数位数的数字
     */
    @JvmStatic
    fun Double.scale(scale: Int = DEFAULT_SCALE): Double {
        require(scale >= 0) { "小数位数不能为负数" }
        val scaleFactor: Double = 10.0.pow(scale.toDouble())
        return Math.round(this * scaleFactor) / scaleFactor
    }

}