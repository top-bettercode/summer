package top.bettercode.summer.tools.optimal.solver.`var`

import com.fasterxml.jackson.annotation.JsonIgnore

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
     * 下限
     */
    val lb: Double

    /**
     * 上限
     */
    val ub: Double

    /**
     * 设置系数
     *  @param coeff 系数
     *  @return 带coeff系数的新变量
     */
    operator fun times(coeff: Double): IVar

    /**
     * 除
     */
    operator fun div(coeff: Double): IVar {
        return this * (1.0 / coeff)
    }


    /**
     * 获取委托变量
     */
    @JsonIgnore
    fun <T> getDelegate(): T

}