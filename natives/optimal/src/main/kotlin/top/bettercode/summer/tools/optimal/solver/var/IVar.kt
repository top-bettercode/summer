package top.bettercode.summer.tools.optimal.solver.`var`

import com.fasterxml.jackson.annotation.JsonIgnore
import top.bettercode.summer.tools.optimal.solver.Constraint
import top.bettercode.summer.tools.optimal.solver.Sense

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

    fun const(sense: Sense, value: Double): Constraint {
        return Constraint(this, sense, value)
    }

    fun eqConst(value: Double): Constraint {
        return Constraint(this, Sense.EQ, value)
    }

    fun neConst(value: Double): Constraint {
        return Constraint(this, Sense.NE, value)
    }

    fun geConst(value: Double): Constraint {
        return Constraint(this, Sense.GE, value)
    }

    fun gtConst(value: Double): Constraint {
        return Constraint(this, Sense.GT, value)
    }

    fun leConst(value: Double): Constraint {
        return Constraint(this, Sense.LE, value)
    }

    fun ltConst(value: Double): Constraint {
        return Constraint(this, Sense.LT, value)
    }


}