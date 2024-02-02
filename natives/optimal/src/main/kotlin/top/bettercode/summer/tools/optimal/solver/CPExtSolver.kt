package top.bettercode.summer.tools.optimal.solver

import com.google.ortools.Loader
import com.google.ortools.sat.*
import top.bettercode.summer.tools.optimal.solver.`var`.CPVar
import top.bettercode.summer.tools.optimal.solver.`var`.IVar
import kotlin.math.pow

/**
 * @author Peter Wu
 */
open class CPExtSolver @JvmOverloads constructor(
        /**
         * 变量放大倍数
         */
        private val times: Int = 0,
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "CPSolver"
) : Solver(name, epsilon) {

    companion object {
        init {
            Loader.loadNativeLibraries()
        }
    }

    val solver: CpSolver = CpSolver()
    val model: CpModel = CpModel()

    var resultStatus: CpSolverStatus = CpSolverStatus.UNKNOWN

    private var numVariables: Int = 0
    private var numConstraints: Int = 0

    private fun enlarge(value: Double): Long {
        val long = (value * 10.0.pow(times) * 10.0.pow(times)).toLong()
        return if (long < Integer.MIN_VALUE) {
            Integer.MIN_VALUE.toLong()
        } else if (long > Integer.MAX_VALUE) {
            Integer.MAX_VALUE.toLong()
        } else {
            long
        }
    }

    private fun enlarge(value: IVar): IntVar {
        return value.getDelegate()
    }

    override fun setTimeLimit(seconds: Int) {
        solver.parameters.maxTimeInSeconds = seconds.toDouble()
    }

    override fun solve() {
        resultStatus = solver.solve(model)
    }

    override fun clear() {
        model.clearObjective()
        model.clearAssumptions()
        model.clearHints()
    }

    override fun isOptimal(): Boolean {
        return CpSolverStatus.OPTIMAL == resultStatus
    }

    override fun getResultStatus(): String {
        return resultStatus.name
    }

    override fun numVariables(): Int {
        return numVariables
    }

    override fun numConstraints(): Int {
        return numConstraints
    }

    override fun boolVarArray(count: Int): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            this.numVariables++
            array[i] = CPVar(_delegate = model.newBoolVar("b" + (numVariables + i + 1)), times = 0, solver = solver)
        }
        return array.requireNoNulls()
    }


    override fun intVarArray(count: Int, lb: Double, ub: Double): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            this.numVariables++
            array[i] = CPVar(model.newIntVar(enlarge(lb), enlarge(ub), "i" + (numVariables + i + 1)), times = times, solver = solver)
        }
        return array.requireNoNulls()
    }

    override fun numVarArray(count: Int, lb: Double, ub: Double): Array<out IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            this.numVariables++
            array[i] = CPVar(model.newIntVar(enlarge(lb), enlarge(ub), "i" + (numVariables + i + 1)), times = times, solver = solver)
        }
        return array.requireNoNulls()
    }

    override fun boolVar(): IVar {
        val numVariables = numVariables()
        this.numVariables++
        return CPVar(_delegate = model.newBoolVar("b" + (numVariables + 1)), times = 0, solver = solver)
    }

    override fun intVar(lb: Double, ub: Double): IVar {
        val numVariables = numVariables()
        this.numVariables++
        return CPVar(_delegate = model.newIntVar(enlarge(lb), enlarge(ub), "i" + (numVariables + 1)), times = times, solver = solver)
    }

    override fun numVar(lb: Double, ub: Double): IVar {
        val numVariables = numVariables()
        this.numVariables++
        return CPVar(_delegate = model.newIntVar(enlarge(lb), enlarge(ub), "i" + (numVariables + 1)), times = times, solver = solver)
    }

    override fun Array<out IVar>.ge(lb: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addGreaterOrEqual(sumExpr, enlarge(lb))
        numConstraints++
    }

    override fun Iterable<IVar>.ge(lb: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addGreaterOrEqual(sumExpr, enlarge(lb))
        numConstraints++
    }

    override fun Array<out IVar>.gt(lb: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addGreaterThan(sumExpr, enlarge(lb))
        numConstraints++
    }

    override fun Iterable<IVar>.gt(lb: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addGreaterThan(sumExpr, enlarge(lb))
        numConstraints++
    }

    /**
     * this>=lb
     * this-lb>=0
     */
    override fun Array<out IVar>.ge(lb: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addGreaterOrEqual(sumExpr, enlarge(lb))
        numConstraints++
    }

    override fun Iterable<IVar>.ge(lb: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addGreaterOrEqual(sumExpr, enlarge(lb))
        numConstraints++
    }


    override fun Array<out IVar>.gt(lb: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addGreaterThan(sumExpr, enlarge(lb))
        numConstraints++
    }

    override fun Iterable<IVar>.gt(lb: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addGreaterThan(sumExpr, enlarge(lb))
        numConstraints++
    }

    override fun Array<out IVar>.le(ub: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addLessOrEqual(sumExpr, enlarge(ub))
        numConstraints++
    }

    override fun Iterable<IVar>.le(ub: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addLessOrEqual(sumExpr, enlarge(ub))
        numConstraints++
    }

    override fun Array<out IVar>.lt(ub: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addLessThan(sumExpr, enlarge(ub))
        numConstraints++
    }

    override fun Iterable<IVar>.lt(ub: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addLessThan(sumExpr, enlarge(ub))
        numConstraints++
    }

    /**
     *  this<=ub
     *  this-ub<=0
     */
    override fun Array<out IVar>.le(ub: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addLessOrEqual(sumExpr, enlarge(ub))
        numConstraints++
    }

    override fun Iterable<IVar>.le(ub: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addLessOrEqual(sumExpr, enlarge(ub))
        numConstraints++
    }

    override fun Array<out IVar>.lt(ub: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addLessThan(sumExpr, enlarge(ub))
        numConstraints++
    }

    override fun Iterable<IVar>.lt(ub: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addLessThan(sumExpr, enlarge(ub))
        numConstraints++
    }

    override fun Array<out IVar>.eq(value: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addEquality(sumExpr, enlarge(value))
        numConstraints++
    }

    override fun Iterable<IVar>.eq(value: Double) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addEquality(sumExpr, enlarge(value))
        numConstraints++
    }

    override fun Array<out IVar>.eq(value: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addEquality(sumExpr, enlarge(value))
        numConstraints++
    }

    override fun Iterable<IVar>.eq(value: IVar) {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        model.addEquality(sumExpr, enlarge(value))
        numConstraints++
    }

    /**
     * 两数之间
     */
    override fun Array<out IVar>.between(lb: Double, ub: Double) {
        this.ge(lb)
        this.le(ub)
    }

    override fun Iterable<IVar>.between(lb: Double, ub: Double) {
        this.ge(lb)
        this.le(ub)
    }

    /**
     * lb<=this<=ub
     * this-lb>=0
     * this-ub<=0
     */
    override fun Array<out IVar>.between(lb: IVar, ub: IVar) {
        this.ge(lb)
        this.le(ub)
    }

    override fun Iterable<IVar>.between(lb: IVar, ub: IVar) {
        this.ge(lb)
        this.le(ub)
    }

    override fun IVar.ge(lb: Double) {
        model.addGreaterOrEqual(this.getDelegate<IntVar>(), enlarge(lb))
        numConstraints++
    }

    override fun IVar.gt(lb: Double) {
        model.addGreaterThan(this.getDelegate<IntVar>(), enlarge(lb))
        numConstraints++
    }

    /**
     * this>=lb
     * this-lb>=0
     */
    override fun IVar.ge(lb: IVar) {
        model.addGreaterOrEqual(this.getDelegate<IntVar>(), enlarge(lb))
        numConstraints++
    }

    override fun IVar.gt(lb: IVar) {
        model.addGreaterThan(this.getDelegate<IntVar>(), enlarge(lb))
        numConstraints++
    }

    override fun IVar.le(ub: Double) {
        model.addLessOrEqual(this.getDelegate<IntVar>(), enlarge(ub))
        numConstraints++
    }

    override fun IVar.lt(ub: Double) {
        model.addLessThan(this.getDelegate<IntVar>(), enlarge(ub))
        numConstraints++
    }

    /**
     * this<=ub
     */
    override fun IVar.le(ub: IVar) {
        model.addLessOrEqual(this.getDelegate<IntVar>(), enlarge(ub))
        numConstraints++
    }

    override fun IVar.lt(ub: IVar) {
        model.addLessThan(this.getDelegate<IntVar>(), enlarge(ub))
        numConstraints++
    }

    override fun IVar.eq(value: Double) {
        model.addEquality(this.getDelegate<IntVar>(), enlarge(value))
        numConstraints++
    }

    override fun IVar.eq(value: IVar) {
        model.addEquality(this.getDelegate<IntVar>(), enlarge(value))
        numConstraints++
    }

    override fun IVar.between(lb: Double, ub: Double) {
        ge(lb)
        le(ub)
    }

    override fun IVar.between(lb: IVar, ub: IVar) {
        ge(lb)
        le(ub)
    }

    /**
     * <pre>
     * bool为1时：var >= value
     *
     * value <= var <= dub
     * dlb*(1-bool) + value*bool <= var <= dub
     * dlb <= var - value*bool + dlb*bool
     * dlb <= var - (value-dlb)*bool
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     *
     */
    override fun IVar.geIf(value: Double, bool: IVar) {
        model.addGreaterOrEqual(this.getDelegate<IntVar>(), enlarge(value)).onlyEnforceIf(bool.getDelegate<BoolVar>())
        numConstraints++
    }

    override fun IVar.gtIf(value: Double, bool: IVar) {
        model.addGreaterThan(this.getDelegate<IntVar>(), enlarge(value)).onlyEnforceIf(bool.getDelegate<BoolVar>())
        numConstraints++
    }


    /**
     * <pre>
     * bool为0时：var >= value
     *
     * value <= var <= dub
     * dlb*bool + value*(1-bool) <= var <= dub
     * value <= var + value*bool - dlb*bool
     * value <= var + (value-dlb)*bool
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     *
     */
    override fun IVar.geIfNot(value: Double, bool: IVar) {
        model.addGreaterOrEqual(this.getDelegate<IntVar>(), enlarge(value)).onlyEnforceIf(bool.getDelegate<BoolVar>().not())
        numConstraints++
    }

    override fun IVar.gtIfNot(value: Double, bool: IVar) {
        model.addGreaterThan(this.getDelegate<IntVar>(), enlarge(value)).onlyEnforceIf(bool.getDelegate<BoolVar>().not())
        numConstraints++
    }


    /**
     * <pre>
     * bool为1时：var <= value
     *
     * dlb <= var <= value
     * dlb <= var <= value*bool + dub*(1-bool)
     * var <= value*bool + dub*(1-bool)
     * var - (value-dub)*bool <=  dub
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     *
     */
    override fun IVar.leIf(value: Double, bool: IVar) {
        model.addLessOrEqual(this.getDelegate<IntVar>(), enlarge(value)).onlyEnforceIf(bool.getDelegate<BoolVar>())
        numConstraints++
    }

    override fun IVar.ltIf(value: Double, bool: IVar) {
        model.addLessThan(this.getDelegate<IntVar>(), enlarge(value)).onlyEnforceIf(bool.getDelegate<BoolVar>())
        numConstraints++
    }


    /**
     * <pre>
     * bool为0时：var <= value
     *
     * dlb <= var <= value
     * dlb <= var <= value*(1-bool) + dub*bool
     * var + (value-dub)*bool <=  value
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     *
     */
    override fun IVar.leIfNot(value: Double, bool: IVar) {
        model.addLessOrEqual(this.getDelegate<IntVar>(), enlarge(value)).onlyEnforceIf(bool.getDelegate<BoolVar>().not())
        numConstraints++
    }

    override fun IVar.ltIfNot(value: Double, bool: IVar) {
        model.addLessThan(this.getDelegate<IntVar>(), enlarge(value)).onlyEnforceIf(bool.getDelegate<BoolVar>().not())
        numConstraints++
    }


    /**
     * <pre>
     * bool为1时：var == value
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.eqIf(value: Double, bool: IVar) {
        geIf(value, bool)
        leIf(value, bool)
    }

    /**
     * <pre>
     * bool为0时：var == value
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.eqIfNot(value: Double, bool: IVar) {
        geIfNot(value, bool)
        leIfNot(value, bool)
    }

    /**
     * <pre>
     * bool为1时：lb <= var <= ub
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.betweenIf(lb: Double, ub: Double, bool: IVar) {
        geIf(lb, bool)
        leIf(ub, bool)
    }


    /**
     * <pre>
     * bool为0时：lb <= var <= ub
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.betweenIfNot(lb: Double, ub: Double, bool: IVar) {
        geIfNot(lb, bool)
        leIfNot(ub, bool)
    }

    override fun Array<out IVar>.sum(): IVar {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        val sum = numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
        val delegate = sum.getDelegate<IntVar>()
        model.addEquality(sumExpr, delegate)
        numConstraints++
        return CPVar(_delegate = delegate, times = times, solver = solver)
    }

    override fun Iterable<IVar>.sum(): IVar {
        val sumExpr: LinearExpr = LinearExpr.weightedSum(this.map { it.getDelegate<IntVar>() }.toTypedArray(), this.map { it.coeff.toLong() }.toLongArray())
        val sum = numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
        val delegate = sum.getDelegate<IntVar>()
        model.addEquality(sumExpr, delegate)
        numConstraints++
        return CPVar(_delegate = delegate, times = times, solver = solver)
    }

    override fun Array<out IVar>.minimize(): IVar {
        val sum = sum()
        model.minimize(sum.getDelegate<IntVar>())
        return sum
    }

    override fun Iterable<IVar>.minimize(): IVar {
        val sum = sum()
        model.minimize(sum.getDelegate<IntVar>())
        return sum
    }

    override fun Array<out IVar>.maximize(): IVar {
        val sum = sum()
        model.maximize(sum.getDelegate<IntVar>())
        return sum
    }

    override fun Iterable<IVar>.maximize(): IVar {
        val sum = sum()
        model.maximize(sum.getDelegate<IntVar>())
        return sum
    }

    override fun Array<out IVar>.atMost(n: Int) {
        val count = size
        if (n >= count) {
            return
        }
        val boolVarArray = boolVarArray(count)
        val sumExpr: LinearExpr = LinearExpr.sum(boolVarArray.map { it.getDelegate<IntVar>() }.toTypedArray())
        model.addEquality(sumExpr, n.toLong())
        for ((i, it) in this.withIndex()) {
            it.leIfNot(0.0, boolVarArray[i])
        }
    }

    override fun Collection<IVar>.atMost(n: Int) {
        val count = size
        if (n >= count) {
            return
        }
        val boolVarArray = boolVarArray(count)
        val sumExpr: LinearExpr = LinearExpr.sum(boolVarArray.map { it.getDelegate<IntVar>() }.toTypedArray())
        model.addEquality(sumExpr, n.toLong())
        for ((i, it) in this.withIndex()) {
            it.leIfNot(0.0, boolVarArray[i])
        }
    }

    override fun Array<out IVar>.atLeast(n: Int, gt: Boolean) {
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
        val sumExpr: LinearExpr = LinearExpr.sum(boolVarArray.map { it.getDelegate<IntVar>() }.toTypedArray())
        model.addEquality(sumExpr, n.toLong())
        for ((i, it) in this.withIndex()) {
            if (gt)
                it.gtIf(0.0, boolVarArray[i])
            else
                it.ltIf(0.0, boolVarArray[i])
        }
    }

    override fun Collection<IVar>.atLeast(n: Int, gt: Boolean) {
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
        val sumExpr: LinearExpr = LinearExpr.sum(boolVarArray.map { it.getDelegate<IntVar>() }.toTypedArray())
        model.addEquality(sumExpr, n.toLong())
        for ((i, it) in this.withIndex()) {
            if (gt)
                it.gtIf(0.0, boolVarArray[i])
            else
                it.ltIf(0.0, boolVarArray[i])
        }
    }
}
