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

    fun gt(vars: Array<IVar>, lb: Double) {
        ge(vars, lb + epsilon)
    }

    abstract fun ge(vars: Array<IVar>, lb: IVar)
    abstract fun gt(vars: Array<IVar>, lb: IVar)
    abstract fun le(vars: Array<IVar>, ub: Double)

    fun lt(vars: Array<IVar>, ub: Double) {
        le(vars, ub - epsilon)
    }

    abstract fun le(vars: Array<IVar>, ub: IVar)
    abstract fun lt(vars: Array<IVar>, ub: IVar)
    abstract fun eq(vars: Array<IVar>, value: Double)
    abstract fun eq(vars: Array<IVar>, value: IVar)
    abstract fun between(vars: Array<IVar>, lb: Double, ub: Double)
    abstract fun between(vars: Array<IVar>, lb: IVar, ub: IVar)
    abstract fun ge(`var`: IVar, lb: Double)

    fun gt(`var`: IVar, lb: Double) {
        ge(`var`, lb + epsilon)
    }

    abstract fun ge(`var`: IVar, lb: IVar)
    abstract fun gt(`var`: IVar, lb: IVar)
    abstract fun le(`var`: IVar, ub: Double)

    fun lt(`var`: IVar, ub: Double) {
        le(`var`, ub - epsilon)
    }

    abstract fun le(`var`: IVar, ub: IVar)
    abstract fun lt(`var`: IVar, ub: IVar)
    abstract fun eq(`var`: IVar, value: Double)
    abstract fun eq(`var`: IVar, value: IVar)
    abstract fun between(`var`: IVar, lb: Double, ub: Double)
    abstract fun between(`var`: IVar, lb: IVar, ub: IVar)

    /**
     * lb <= dividend/divisor <= ub
     */
    abstract fun between(dividend: IVar, divisor: IVar, lb: Double, ub: Double)
    abstract fun geIf(`var`: IVar, value: Double, bool: IVar)

    fun gtIf(`var`: IVar, value: Double, bool: IVar) {
        geIf(`var`, value + epsilon, bool)
    }

    abstract fun geIfNot(`var`: IVar, value: Double, bool: IVar)

    fun gtIfNot(`var`: IVar, value: Double, bool: IVar) {
        geIfNot(`var`, value + epsilon, bool)
    }

    abstract fun leIf(`var`: IVar, value: Double, bool: IVar)

    fun ltIf(`var`: IVar, value: Double, bool: IVar) {
        leIf(`var`, value - epsilon, bool)
    }

    abstract fun leIfNot(`var`: IVar, value: Double, bool: IVar)

    fun ltIfNot(`var`: IVar, value: Double, bool: IVar) {
        leIfNot(`var`, value - epsilon, bool)
    }

    abstract fun eqIf(`var`: IVar, value: Double, bool: IVar)
    abstract fun eqIfNot(`var`: IVar, value: Double, bool: IVar)
    abstract fun betweenIf(`var`: IVar, lb: Double, ub: Double, bool: IVar)
    abstract fun betweenIfNot(`var`: IVar, lb: Double, ub: Double, bool: IVar)
    abstract fun sum(vars: Array<IVar>): IVar
    abstract fun minimize(vars: Array<IVar>): IVar
    abstract fun maximize(vars: Array<IVar>): IVar

    /**
     * 最多1个非零变量,至少size-1个零
     */
    open fun atMostOne(vars: Array<IVar>) {
        atMost(vars, 1)
    }

    /**
     * 最多n个非零变量,至少size-n个零
     */
    fun atMost(vars: Array<IVar>, n: Int) {
        val count = vars.size
        if (n >= count) {
            return
        }
        val boolVarArray = boolVarArray(count)
        eq(boolVarArray, n.toDouble())
        for (i in vars.indices) {
            eqIfNot(vars[i], 0.0, boolVarArray[i])
        }
    }

    /**
     * 最少1个非零变量,最多size-1个零
     */
    @JvmOverloads
    fun atLeastOne(vars: Array<IVar>, gt: Boolean = true) {
        atLeast(vars, 1, gt)
    }

    /**
     * 最少n个非零变量,最多size-n个零
     */
    @JvmOverloads
    fun atLeast(vars: Array<IVar>, n: Int, gt: Boolean = true) {
        val count = vars.size
        if (n >= count) {
            for (i in vars.indices) {
                if (gt)
                    gt(vars[i], 0.0)
                else
                    lt(vars[i], 0.0)
            }
            return
        }
        val boolVarArray = boolVarArray(count)
        eq(boolVarArray, n.toDouble())
        for (i in vars.indices) {
            if (gt)
                gtIf(vars[i], 0.0, boolVarArray[i])
            else
                ltIf(vars[i], 0.0, boolVarArray[i])
        }
    }

    abstract fun setTimeLimit(seconds: Int)
    abstract fun solve()
    abstract fun clear()
    abstract fun isOptimal(): Boolean
    abstract fun getResultStatus(): String
    abstract fun numVariables(): Int
    abstract fun numConstraints(): Int
}