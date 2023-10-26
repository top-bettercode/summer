package top.bettercode.summer.tools.optimal.solver.`var`

import kotlin.math.pow

/**
 *
 * @author Peter Wu
 */
interface IVar {

    /**
     * 求解系数
     */
    val coeff: Double

    /**
     * 求解结果
     */
    val value: Double

    /**
     * 设置系数
     *  @param coeff 系数
     *  @return 带coeff系数的新变量
     */
    fun coeff(coeff: Double): IVar

    /**
     * 获取委托变量
     */
    fun <T> getDelegate(): T

    companion object {
        /**
         * 求解结果小数位
         */
        var decimalPlaces: Int = 12

        /**
         * 将数字转换为指定小数位数的数字
         *
         * @param number        数字
         * @param decimalPlaces 小数位数
         * @return 指定小数位数的数字
         */
        @JvmStatic
        @JvmOverloads
        fun convertDecimal(number: Double, decimalPlaces: Int = IVar.decimalPlaces): Double {
            require(decimalPlaces >= 0) { "小数位数不能为负数" }
            val scaleFactor: Double = 10.0.pow(decimalPlaces.toDouble())
            return Math.round(number * scaleFactor) / scaleFactor
        }
    }
}