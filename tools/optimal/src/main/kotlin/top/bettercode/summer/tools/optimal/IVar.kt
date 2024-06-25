package top.bettercode.summer.tools.optimal

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 *
 * @author Peter Wu
 */
interface IVar {

    val isInt: Boolean

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
    var lb: Double

    /**
     * 上限
     */
    var ub: Double

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

    fun expr(operator: Operator, value: Double): Expr {
        return Expr(this, operator, value)
    }

    fun eqExpr(value: Double): Expr {
        return Expr(this, Operator.EQ, value)
    }

    fun neExpr(value: Double): Expr {
        return Expr(this, Operator.NE, value)
    }

    fun geExpr(value: Double): Expr {
        return Expr(this, Operator.GE, value)
    }

    fun gtExpr(value: Double): Expr {
        return Expr(this, Operator.GT, value)
    }

    fun leExpr(value: Double): Expr {
        return Expr(this, Operator.LE, value)
    }

    fun ltExpr(value: Double): Expr {
        return Expr(this, Operator.LT, value)
    }


}