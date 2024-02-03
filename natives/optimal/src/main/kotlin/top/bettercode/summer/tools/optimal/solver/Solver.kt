package top.bettercode.summer.tools.optimal.solver

import copt.Consts
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
        protected val epsilon: Double = OptimalUtil.DEFAULT_EPSILON
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
    abstract fun boolVar(name: String? = null): IVar
    abstract fun intVar(lb: Double = -Consts.INFINITY, ub: Double = Consts.INFINITY, name: String? = null): IVar
    abstract fun numVar(lb: Double = -Consts.INFINITY, ub: Double = Consts.INFINITY, name: String? = null): IVar
    fun boolVarArray(count: Int): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = boolVar("b" + (numVariables + i + 1))
        }
        return array.requireNoNulls()
    }

    fun intVarArray(count: Int, lb: Double = -Consts.INFINITY, ub: Double = Consts.INFINITY): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = intVar(lb, ub, "i" + (numVariables + i + 1))
        }
        return array.requireNoNulls()
    }

    fun numVarArray(count: Int, lb: Double = -Consts.INFINITY, ub: Double = Consts.INFINITY): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = numVar(lb, ub, "n" + (numVariables + i + 1))
        }
        return array.requireNoNulls()
    }

    open operator fun IVar.plus(value: Double): IVar {
        return arrayOf(this, numVar(1.0, 1.0) * value).sum()
    }

    open operator fun IVar.plus(value: IVar): IVar {
        return arrayOf(this, value).sum()
    }

    open operator fun IVar.minus(value: Double): IVar {
        return this + (-value)
    }

    open operator fun IVar.minus(value: IVar): IVar {
        return arrayOf(this, value * -1.0).sum()
    }

    abstract fun IVar.ge(lb: Double)
    abstract fun IVar.ge(lb: IVar)
    abstract fun Array<out IVar>.ge(lb: Double)
    abstract fun Iterable<IVar>.ge(lb: Double)
    abstract fun Array<out IVar>.ge(lb: IVar)
    abstract fun Iterable<IVar>.ge(lb: IVar)

    open fun IVar.gt(lb: Double) {
        ge(lb + epsilon)
    }

    abstract fun IVar.gt(lb: IVar)

    open fun Array<out IVar>.gt(lb: Double) {
        ge(lb + epsilon)
    }

    open fun Iterable<IVar>.gt(lb: Double) {
        ge(lb + epsilon)
    }

    abstract fun Array<out IVar>.gt(lb: IVar)
    abstract fun Iterable<IVar>.gt(lb: IVar)
    abstract fun IVar.le(ub: Double)
    abstract fun IVar.le(ub: IVar)
    abstract fun Array<out IVar>.le(ub: Double)
    abstract fun Iterable<IVar>.le(ub: Double)
    abstract fun Array<out IVar>.le(ub: IVar)
    abstract fun Iterable<IVar>.le(ub: IVar)

    open fun IVar.lt(ub: Double) {
        le(ub - epsilon)
    }

    abstract fun IVar.lt(ub: IVar)
    open fun Array<out IVar>.lt(ub: Double) {
        le(ub - epsilon)
    }

    open fun Iterable<IVar>.lt(ub: Double) {
        le(ub - epsilon)
    }

    abstract fun Array<out IVar>.lt(ub: IVar)
    abstract fun Iterable<IVar>.lt(ub: IVar)


    abstract fun IVar.eq(value: Double)
    abstract fun IVar.eq(value: IVar)

    abstract fun Array<out IVar>.eq(value: Double)
    abstract fun Iterable<IVar>.eq(value: Double)
    abstract fun Array<out IVar>.eq(value: IVar)
    abstract fun Iterable<IVar>.eq(value: IVar)

    open fun IVar.ne(value: Double) {
        val boolVar = boolVar()
        gtIf(value, boolVar)
        ltIfNot(value, boolVar)
    }


    abstract fun IVar.between(lb: Double, ub: Double)
    abstract fun IVar.between(lb: IVar, ub: IVar)

    abstract fun Array<out IVar>.between(lb: Double, ub: Double)
    abstract fun Iterable<IVar>.between(lb: Double, ub: Double)
    abstract fun Array<out IVar>.between(lb: IVar, ub: IVar)
    abstract fun Iterable<IVar>.between(lb: IVar, ub: IVar)

    open fun IVar.isTrue() {
        eq(1.0)
    }

    open fun IVar.isFalse() {
        eq(0.0)
    }

    /**
     * lb <= this/whole <= ub
     * whole*lb <= this
     * this   <= whole*ub
     * whole*ub >= this
     */
    fun IVar.ratioInRange(whole: IVar, lb: Double, ub: Double) {
        (whole * lb).le(this)
        (whole * ub).ge(this)
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

    abstract fun IVar.leIfNot(value: Double, bool: IVar)
    open fun IVar.ltIf(value: Double, bool: IVar) {
        leIf(value - epsilon, bool)
    }

    open fun IVar.ltIfNot(value: Double, bool: IVar) {
        leIfNot(value - epsilon, bool)
    }

    abstract fun IVar.eqIf(value: Double, bool: IVar)
    abstract fun IVar.eqIfNot(value: Double, bool: IVar)

    /**
     * if bool=1,this<value,this>value
     *
     */
    open fun IVar.neIf(value: Double, bool: IVar) {
        log.warn("MPSolver eqIf experimental")
        val bool1 = boolVar()
        val bool2 = boolVar()
        arrayOf(bool1, bool2).sum().geIf(1.0, bool)
        gtIf(value, bool1)
        ltIf(value, bool2)
    }

    /**
     * if bool=0,this<value,this>value
     *
     */
    open fun IVar.neIfNot(value: Double, bool: IVar) {
        log.warn("MPSolver eqIf experimental")
        val bool1 = boolVar()
        val bool2 = boolVar()
        arrayOf(bool1, bool2).sum().geIfNot(1.0, bool)
        gtIf(value, bool1)
        ltIf(value, bool2)
    }

    abstract fun IVar.betweenIf(lb: Double, ub: Double, bool: IVar)
    abstract fun IVar.betweenIfNot(lb: Double, ub: Double, bool: IVar)
    open fun Constraint.onlyEnforceIf(condition: Constraint) {
        val boolVar = boolVar()
        val whenVariable = condition.variable
        val whenValue = condition.value
        when (condition.sense) {
            Sense.EQ -> {
                whenVariable.neIfNot(whenValue, boolVar)
            }

            Sense.NE -> {
                whenVariable.eqIfNot(whenValue, boolVar)
            }

            Sense.GT -> {
                whenVariable.leIfNot(whenValue, boolVar)
            }

            Sense.LT -> {
                whenVariable.geIfNot(whenValue, boolVar)
            }

            Sense.GE -> {
                whenVariable.ltIfNot(whenValue, boolVar)
            }

            Sense.LE -> {
                whenVariable.gtIfNot(whenValue, boolVar)
            }
        }
        val thenVar = this.variable
        val thenValue = this.value
        when (this.sense) {
            Sense.EQ -> thenVar.eqIf(thenValue, boolVar)
            Sense.NE -> thenVar.neIf(thenValue, boolVar)
            Sense.GT -> thenVar.gtIf(thenValue, boolVar)
            Sense.LT -> thenVar.ltIf(thenValue, boolVar)
            Sense.GE -> thenVar.geIf(thenValue, boolVar)
            Sense.LE -> thenVar.leIf(thenValue, boolVar)
        }
    }

    open fun Array<Constraint>.onlyEnforceIf(condition: Constraint) {
        val boolVar = boolVar()
        val whenVariable = condition.variable
        val whenValue = condition.value
        when (condition.sense) {
            Sense.EQ -> {
                whenVariable.neIfNot(whenValue, boolVar)
            }

            Sense.NE -> {
                whenVariable.eqIfNot(whenValue, boolVar)
            }

            Sense.GT -> {
                whenVariable.leIfNot(whenValue, boolVar)
            }

            Sense.LT -> {
                whenVariable.geIfNot(whenValue, boolVar)
            }

            Sense.GE -> {
                whenVariable.ltIfNot(whenValue, boolVar)
            }

            Sense.LE -> {
                whenVariable.gtIfNot(whenValue, boolVar)
            }
        }
        this.forEach {
            val thenVar = it.variable
            val thenValue = it.value
            when (it.sense) {
                Sense.EQ -> thenVar.eqIf(thenValue, boolVar)
                Sense.NE -> thenVar.neIf(thenValue, boolVar)
                Sense.GT -> thenVar.gtIf(thenValue, boolVar)
                Sense.LT -> thenVar.ltIf(thenValue, boolVar)
                Sense.GE -> thenVar.geIf(thenValue, boolVar)
                Sense.LE -> thenVar.leIf(thenValue, boolVar)
            }
        }
    }

    open fun Iterable<Constraint>.onlyEnforceIf(condition: Constraint) {
        val boolVar = boolVar()
        val whenVariable = condition.variable
        val whenValue = condition.value
        when (condition.sense) {
            Sense.EQ -> {
                whenVariable.neIfNot(whenValue, boolVar)
            }

            Sense.NE -> {
                whenVariable.eqIfNot(whenValue, boolVar)
            }

            Sense.GT -> {
                whenVariable.leIfNot(whenValue, boolVar)
            }

            Sense.LT -> {
                whenVariable.geIfNot(whenValue, boolVar)
            }

            Sense.GE -> {
                whenVariable.ltIfNot(whenValue, boolVar)
            }

            Sense.LE -> {
                whenVariable.gtIfNot(whenValue, boolVar)
            }
        }
        this.forEach {
            val thenVar = it.variable
            val thenValue = it.value
            when (it.sense) {
                Sense.EQ -> thenVar.eqIf(thenValue, boolVar)
                Sense.NE -> thenVar.neIf(thenValue, boolVar)
                Sense.GT -> thenVar.gtIf(thenValue, boolVar)
                Sense.LT -> thenVar.ltIf(thenValue, boolVar)
                Sense.GE -> thenVar.geIf(thenValue, boolVar)
                Sense.LE -> thenVar.leIf(thenValue, boolVar)
            }
        }
    }

    abstract fun Array<out IVar>.sum(): IVar
    abstract fun Iterable<IVar>.sum(): IVar
    abstract fun Array<out IVar>.minimize(): IVar
    abstract fun Iterable<IVar>.minimize(): IVar
    abstract fun Array<out IVar>.maximize(): IVar
    abstract fun Iterable<IVar>.maximize(): IVar

    /**
     * 最多1个非零变量,至少size-1个零
     */
    open fun Array<out IVar>.atMostOne() {
        atMost(1)
    }

    open fun Collection<IVar>.atMostOne() {
        atMost(1)
    }

    /**
     * 最多n个>零变量,至少size-n个零
     */
    open fun Array<out IVar>.atMost(n: Int) {
        val count = size
        if (n >= count) {
            return
        }
        val boolVarArray = boolVarArray(count)
        boolVarArray.eq(n.toDouble())
        for ((i, it) in this.withIndex()) {
            it.leIfNot(0.0, boolVarArray[i])
        }
    }

    open fun Collection<IVar>.atMost(n: Int) {
        val count = size
        if (n >= count) {
            return
        }
        val boolVarArray = boolVarArray(count)
        boolVarArray.eq(n.toDouble())
        for ((i, it) in this.withIndex()) {
            it.leIfNot(0.0, boolVarArray[i])
        }
    }

    /**
     * 最少1个非零变量,最多size-1个零
     */
    @JvmOverloads
    fun Array<out IVar>.atLeastOne(gt: Boolean = true) {
        atLeast(1, gt)
    }

    fun Collection<IVar>.atLeastOne(gt: Boolean = true) {
        atLeast(1, gt)
    }

    /**
     * 最少n个非零变量,最多size-n个零
     */
    @JvmOverloads
    open fun Array<out IVar>.atLeast(n: Int, gt: Boolean = true) {
        val count = size
        if (n >= count) {
            for (it in this) {
                if (gt)
                    it.gt(0.0)
                else
                    it.lt(0.0)
            }
            return
        }
        val boolVarArray = boolVarArray(count)
        boolVarArray.eq(n.toDouble())
        for ((i, it) in this.withIndex()) {
            if (gt)
                it.gtIf(0.0, boolVarArray[i])
            else
                it.ltIf(0.0, boolVarArray[i])
        }
    }

    open fun Collection<IVar>.atLeast(n: Int, gt: Boolean = true) {
        val count = size
        if (n >= count) {
            for (it in this) {
                if (gt)
                    it.gt(0.0)
                else
                    it.lt(0.0)
            }
            return
        }
        val boolVarArray = boolVarArray(count)
        boolVarArray.eq(n.toDouble())
        for ((i, it) in this.withIndex()) {
            if (gt)
                it.gtIf(0.0, boolVarArray[i])
            else
                it.ltIf(0.0, boolVarArray[i])
        }
    }
}