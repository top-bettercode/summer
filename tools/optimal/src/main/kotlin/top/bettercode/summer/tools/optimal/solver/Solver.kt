package top.bettercode.summer.tools.optimal.solver

import top.bettercode.summer.tools.optimal.solver.`var`.IVar

/**
 *
 * @author Peter Wu
 */
abstract class Solver {

    abstract val name: String

    /**
     * 极小的正数，用于描述误差，大于 转换为 大于等于
     */
    var epsilon: Double = 1e-6

    abstract fun boolVarArray(count: Int): Array<IVar>
    abstract fun intVarArray(count: Int, lb: Double, ub: Double): Array<IVar>
    abstract fun numVarArray(count: Int, lb: Double, ub: Double): Array<IVar>
    abstract fun boolVar(): IVar
    abstract fun intVar(lb: Double, ub: Double): IVar
    abstract fun numVar(lb: Double, ub: Double): IVar
    abstract fun ge(vars: Array<IVar>, lb: Double)
    abstract fun ge(vars: Array<IVar>, lb: IVar)
    abstract fun le(vars: Array<IVar>, ub: Double)
    abstract fun le(vars: Array<IVar>, ub: IVar)
    abstract fun eq(vars: Array<IVar>, value: Double)
    abstract fun eq(vars: Array<IVar>, value: IVar)
    abstract fun between(vars: Array<IVar>, lb: Double, ub: Double)
    abstract fun between(vars: Array<IVar>, lb: IVar, ub: IVar)
    abstract fun ge(`var`: IVar, lb: Double)
    abstract fun ge(`var`: IVar, lb: IVar)
    abstract fun le(`var`: IVar, ub: Double)
    abstract fun le(`var`: IVar, ub: IVar)
    abstract fun eq(`var`: IVar, value: Double)
    abstract fun eq(`var`: IVar, value: IVar)
    abstract fun between(`var`: IVar, lb: Double, ub: Double)
    abstract fun between(`var`: IVar, lb: IVar, ub: IVar)

    /**
     * lb <= dividend/divisor <= ub
     */
    abstract fun between(dividend: IVar, divisor: IVar, lb: Double, ub: Double)
    abstract fun geIf(`var`: IVar, value: Double, bool: IVar)
    abstract fun geIfNot(`var`: IVar, value: Double, bool: IVar)
    abstract fun leIf(`var`: IVar, value: Double, bool: IVar)
    abstract fun leIfNot(`var`: IVar, value: Double, bool: IVar)
    abstract fun eqIf(`var`: IVar, value: Double, bool: IVar)
    abstract fun eqIfNot(`var`: IVar, value: Double, bool: IVar)
    abstract fun betweenIf(`var`: IVar, lb: Double, ub: Double, bool: IVar)
    abstract fun betweenIfNot(`var`: IVar, lb: Double, ub: Double, bool: IVar)
    abstract fun sum(vars: Array<IVar>): IVar
    abstract fun minimize(vars: Array<IVar>): IVar
    abstract fun maximize(vars: Array<IVar>): IVar

    /**
     * 最多1个非零变量
     */
    abstract fun atMostOne(vars: Array<IVar>)
    abstract fun atMost(vars: Array<IVar>, n: Int)
    abstract fun setTimeLimit(seconds: Int)
    abstract fun solve()
    abstract fun isOptimal(): Boolean
    abstract fun getResultStatus(): String
    abstract fun numVariables(): Int
    abstract fun numConstraints(): Int
}