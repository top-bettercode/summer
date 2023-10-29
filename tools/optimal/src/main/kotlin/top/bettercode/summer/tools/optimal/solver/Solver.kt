package top.bettercode.summer.tools.optimal.solver

import top.bettercode.summer.tools.optimal.solver.`var`.IVar
import kotlin.math.abs
import kotlin.math.log10

/**
 *
 * @author Peter Wu
 */
abstract class Solver(
        val name: String,

        /**
         * 极小的正数，用于描述误差，大于 转换为 大于等于
         */
        protected val epsilon: Double = 1e-6
) {

    /**
     * 小数点后的位数
     */
    val scale get() = abs(log10(epsilon)).toInt()

    abstract fun setTimeLimit(seconds: Int)
    abstract fun solve()
    abstract fun clear()
    abstract fun isOptimal(): Boolean
    abstract fun getResultStatus(): String
    abstract fun numVariables(): Int
    abstract fun numConstraints(): Int

    abstract fun boolVarArray(count: Int): Array<IVar>
    abstract fun intVarArray(count: Int, lb: Double, ub: Double): Array<IVar>
    abstract fun numVarArray(count: Int, lb: Double, ub: Double): Array<IVar>
    abstract fun boolVar(): IVar
    abstract fun intVar(lb: Double, ub: Double): IVar
    abstract fun numVar(lb: Double, ub: Double): IVar
    abstract fun Array<IVar>.ge(lb: Double)

    fun Array<IVar>.gt(lb: Double) {
        ge(lb + epsilon)
    }

    abstract fun Array<IVar>.ge(lb: IVar)
    abstract fun Array<IVar>.gt(lb: IVar)
    abstract fun Array<IVar>.le(ub: Double)

    fun Array<IVar>.lt(ub: Double) {
        le(ub - epsilon)
    }

    abstract fun Array<IVar>.le(ub: IVar)
    abstract fun Array<IVar>.lt(ub: IVar)
    abstract fun Array<IVar>.eq(value: Double)
    abstract fun Array<IVar>.eq(value: IVar)
    abstract fun Array<IVar>.between(lb: Double, ub: Double)
    abstract fun Array<IVar>.between(lb: IVar, ub: IVar)
    abstract fun IVar.ge(lb: Double)

    fun IVar.gt(lb: Double) {
        ge(lb + epsilon)
    }

    abstract fun IVar.ge(lb: IVar)
    abstract fun IVar.gt(lb: IVar)
    abstract fun IVar.le(ub: Double)

    fun IVar.lt(ub: Double) {
        le(ub - epsilon)
    }

    abstract fun IVar.le(ub: IVar)
    abstract fun IVar.lt(ub: IVar)
    abstract fun IVar.eq(value: Double)
    abstract fun IVar.eq(value: IVar)
    abstract fun IVar.between(lb: Double, ub: Double)
    abstract fun IVar.between(lb: IVar, ub: IVar)

    /**
     * lb <= dividend/divisor <= ub
     * divisor*lb <= dividend
     * dividend   <= divisor*ub
     */
    fun between(dividend: IVar, divisor: IVar, lb: Double, ub: Double) {
        divisor.coeff(lb).le(dividend)
        divisor.coeff(ub).ge(dividend)
    }

    abstract fun IVar.geIf(value: Double, bool: IVar)

    fun IVar.gtIf(value: Double, bool: IVar) {
        geIf(value + epsilon, bool)
    }

    abstract fun IVar.geIfNot(value: Double, bool: IVar)

    fun IVar.gtIfNot(value: Double, bool: IVar) {
        geIfNot(value + epsilon, bool)
    }

    abstract fun IVar.leIf(value: Double, bool: IVar)

    fun IVar.ltIf(value: Double, bool: IVar) {
        leIf(value - epsilon, bool)
    }

    abstract fun IVar.leIfNot(value: Double, bool: IVar)

    fun IVar.ltIfNot(value: Double, bool: IVar) {
        leIfNot(value - epsilon, bool)
    }

    abstract fun IVar.eqIf(value: Double, bool: IVar)
    abstract fun IVar.eqIfNot(value: Double, bool: IVar)
    abstract fun IVar.betweenIf(lb: Double, ub: Double, bool: IVar)
    abstract fun IVar.betweenIfNot(lb: Double, ub: Double, bool: IVar)
    abstract fun Array<IVar>.sum(): IVar
    abstract fun Array<IVar>.minimize(): IVar
    abstract fun Array<IVar>.maximize(): IVar

    /**
     * 最多1个非零变量,至少size-1个零
     */
    open fun Array<IVar>.atMostOne() {
        atMost(1)
    }

    /**
     * 最多n个非零变量,至少size-n个零
     */
    fun Array<IVar>.atMost(n: Int) {
        val count = size
        if (n >= count) {
            return
        }
        val boolVarArray = boolVarArray(count)
        boolVarArray.eq(n.toDouble())
        for (i in indices) {
            this[i].eqIfNot(0.0, boolVarArray[i])
        }
    }

    /**
     * 最少1个非零变量,最多size-1个零
     */
    @JvmOverloads
    fun Array<IVar>.atLeastOne(gt: Boolean = true) {
        atLeast(1, gt)
    }

    /**
     * 最少n个非零变量,最多size-n个零
     */
    @JvmOverloads
    fun Array<IVar>.atLeast(n: Int, gt: Boolean = true) {
        val count = size
        if (n >= count) {
            for (i in indices) {
                if (gt)
                    this[i].gt(0.0)
                else
                    this[i].lt(0.0)
            }
            return
        }
        val boolVarArray = boolVarArray(count)
        boolVarArray.eq(n.toDouble())
        for (i in indices) {
            if (gt)
                this[i].gtIf(0.0, boolVarArray[i])
            else
                this[i].ltIf(0.0, boolVarArray[i])
        }
    }

}