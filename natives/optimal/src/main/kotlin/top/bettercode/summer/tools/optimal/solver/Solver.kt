package top.bettercode.summer.tools.optimal.solver

import org.slf4j.Logger
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

    protected val log: Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass)

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

    abstract fun boolVarArray(count: Int): Array<out IVar>
    abstract fun intVarArray(count: Int, lb: Double, ub: Double): Array<out IVar>
    abstract fun numVarArray(count: Int, lb: Double, ub: Double): Array<out IVar>
    abstract fun boolVar(): IVar
    abstract fun intVar(lb: Double, ub: Double): IVar
    abstract fun numVar(lb: Double, ub: Double): IVar
    abstract fun Array<out IVar>.ge(lb: Double)

    open fun Array<out IVar>.gt(lb: Double) {
        ge(lb + epsilon)
    }

    abstract fun Array<out IVar>.ge(lb: IVar)
    abstract fun Array<out IVar>.gt(lb: IVar)
    abstract fun Array<out IVar>.le(ub: Double)

    open fun Array<out IVar>.lt(ub: Double) {
        le(ub - epsilon)
    }

    abstract fun Array<out IVar>.le(ub: IVar)
    abstract fun Array<out IVar>.lt(ub: IVar)
    abstract fun Array<out IVar>.eq(value: Double)
    abstract fun Array<out IVar>.eq(value: IVar)
    abstract fun Array<out IVar>.between(lb: Double, ub: Double)
    abstract fun Array<out IVar>.between(lb: IVar, ub: IVar)
    abstract fun IVar.ge(lb: Double)

    open fun IVar.gt(lb: Double) {
        ge(lb + epsilon)
    }

    abstract fun IVar.ge(lb: IVar)
    abstract fun IVar.gt(lb: IVar)
    abstract fun IVar.le(ub: Double)

    open fun IVar.lt(ub: Double) {
        le(ub - epsilon)
    }

    abstract fun IVar.le(ub: IVar)
    abstract fun IVar.lt(ub: IVar)

    open fun IVar.isTrue() {
        eq(1.0)
    }

    open fun IVar.isFalse() {
        eq(0.0)
    }

    abstract fun IVar.eq(value: Double)
    abstract fun IVar.eq(value: IVar)
    open fun IVar.ne(value: Double) {
        val boolVar = boolVar()
        gtIf(value, boolVar)
        ltIfNot(value, boolVar)
    }

    abstract fun IVar.between(lb: Double, ub: Double)
    abstract fun IVar.between(lb: IVar, ub: IVar)

    /**
     * lb <= this/whole <= ub
     * whole*lb <= this
     * this   <= whole*ub
     * whole*ub >= this
     */
    fun IVar.ratioInRange(whole: IVar, lb: Double, ub: Double) {
        whole.coeff(lb).le(this)
        whole.coeff(ub).ge(this)
    }

    abstract fun IVar.geIf(value: Double, bool: IVar)

    open fun IVar.gtIf(value: Double, bool: IVar) {
        geIf(value + epsilon, bool)
    }

    abstract fun IVar.geIfNot(value: Double, bool: IVar)

    open fun IVar.gtIfNot(value: Double, bool: IVar) {
        geIfNot(value + epsilon, bool)
    }

    abstract fun IVar.leIf(value: Double, bool: IVar)

    open fun IVar.ltIf(value: Double, bool: IVar) {
        leIf(value - epsilon, bool)
    }

    abstract fun IVar.leIfNot(value: Double, bool: IVar)

    open fun IVar.ltIfNot(value: Double, bool: IVar) {
        leIfNot(value - epsilon, bool)
    }

    abstract fun IVar.eqIf(value: Double, bool: IVar)
    abstract fun IVar.eqIfNot(value: Double, bool: IVar)
    open fun IVar.neIf(value: Double, bool: IVar) {
        throw UnsupportedOperationException("不支持条件不等于约束")
        //if bool=0,this==value
        //if bool=1,this<value,this>value
    }

    open fun IVar.neIfNot(value: Double, bool: IVar) {
        throw UnsupportedOperationException("不支持条件不等于约束")
        //if bool=1,this==value
        //if bool=0,this<value,this>value
    }

    abstract fun IVar.betweenIf(lb: Double, ub: Double, bool: IVar)
    abstract fun IVar.betweenIfNot(lb: Double, ub: Double, bool: IVar)
    abstract fun Array<out IVar>.sum(): IVar
    abstract fun Array<out IVar>.minimize(): IVar
    abstract fun Array<out IVar>.maximize(): IVar

    /**
     * 最多1个非零变量,至少size-1个零
     */
    open fun Array<out IVar>.atMostOne() {
        atMost(1)
    }

    /**
     * 最多n个非零变量,至少size-n个零
     */
    open fun Array<out IVar>.atMost(n: Int) {
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
    fun Array<out IVar>.atLeastOne(gt: Boolean = true) {
        atLeast(1, gt)
    }

    /**
     * 最少n个非零变量,最多size-n个零
     */
    @JvmOverloads
    open fun Array<out IVar>.atLeast(n: Int, gt: Boolean = true) {
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