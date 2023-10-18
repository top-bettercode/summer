package top.bettercode.summer.tools.optimal.solver

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import top.bettercode.summer.tools.optimal.solver.`var`.IVar
import top.bettercode.summer.tools.optimal.solver.`var`.MPObjectiveVar
import top.bettercode.summer.tools.optimal.solver.`var`.MPVar

/**
 * @author Peter Wu
 */
open class MPExtSolver @JvmOverloads constructor(
        /**
         * OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING
         * OptimizationProblemType.SCIP_MIXED_INTEGER_PROGRAMMING
         */
        type: MPSolver.OptimizationProblemType,
        final override val name: String = "MPSolver",
        /**
         * 变量默认下界
         */
        var dlb: Double = 0.0,

        /**
         * 变量默认上界
         */
        var dub: Double = 1000.0) : Solver() {

    companion object {
        init {
            Loader.loadNativeLibraries()
        }
    }

    val solver: MPSolver

    var resultStatus: MPSolver.ResultStatus = MPSolver.ResultStatus.NOT_SOLVED

    init {
        solver = MPSolver(name, type)
    }


    override fun boolVarArray(count: Int): Array<IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = MPVar(solver.makeBoolVar("b" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }


    override fun intVarArray(count: Int, lb: Double, ub: Double): Array<IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = MPVar(solver.makeIntVar(lb, ub, "i" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }

    override fun numVarArray(count: Int, lb: Double, ub: Double): Array<IVar> {
        val array = arrayOfNulls<IVar>(count)
        val numVariables = numVariables()
        for (i in 0 until count) {
            array[i] = MPVar(solver.makeNumVar(lb, ub, "n" + (numVariables + i + 1)))
        }
        return array.requireNoNulls()
    }

    override fun boolVar(): IVar {
        val numVariables = numVariables()
        return MPVar(solver.makeBoolVar("b" + (numVariables + 1)))
    }

    override fun intVar(lb: Double, ub: Double): IVar {
        val numVariables = numVariables()
        return MPVar(solver.makeIntVar(lb, ub, "i" + (numVariables + 1)))
    }

    override fun numVar(lb: Double, ub: Double): IVar {
        val numVariables = numVariables()
        return MPVar(solver.makeNumVar(lb, ub, "n" + (numVariables + 1)))
    }

    override fun ge(vars: Array<IVar>, lb: Double) {
        val constraint = solver.makeConstraint(lb, Double.POSITIVE_INFINITY)
        for (v in vars) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    /**
     * vars>=lb
     * vars-lb>=0
     */
    override fun ge(vars: Array<IVar>, lb: IVar) {
        val constraint = solver.makeConstraint(0.0, Double.POSITIVE_INFINITY)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in vars) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun le(vars: Array<IVar>, ub: Double) {
        val constraint = solver.makeConstraint(Double.NEGATIVE_INFINITY, ub)
        for (v in vars) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    /**
     *  vars<=ub
     *  vars-ub<=0
     */
    override fun le(vars: Array<IVar>, ub: IVar) {
        val constraint = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
        for (v in vars) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun eq(vars: Array<IVar>, value: Double) {
        val constraint = solver.makeConstraint(value, value)
        for (v in vars) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun eq(vars: Array<IVar>, value: IVar) {
        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(value.getDelegate(), -value.coeff)
        for (v in vars) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    /**
     * 两数之间
     */
    override fun between(vars: Array<IVar>, lb: Double, ub: Double) {
        val constraint = solver.makeConstraint(lb, ub)
        for (v in vars) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    /**
     * lb<=vars<=ub
     * vars-lb>=0
     * vars-ub<=0
     */
    override fun between(vars: Array<IVar>, lb: IVar, ub: IVar) {
        ge(vars, lb)
        le(vars, ub)
    }

    override fun ge(`var`: IVar, lb: Double) {
        val constraint = solver.makeConstraint(lb, Double.POSITIVE_INFINITY)
        constraint.setCoefficient(`var`.getDelegate(), `var`.coeff)
    }

    /**
     * `var`>=lb
     * `var`-lb>=0
     */
    override fun ge(`var`: IVar, lb: IVar) {
        val constraint = solver.makeConstraint(0.0, Double.POSITIVE_INFINITY)
        constraint.setCoefficient(`var`.getDelegate(), `var`.coeff)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
    }

    override fun le(`var`: IVar, ub: Double) {
        val constraint = solver.makeConstraint(Double.NEGATIVE_INFINITY, ub)
        constraint.setCoefficient(`var`.getDelegate(), `var`.coeff)
    }

    /**
     * `var`<=ub
     */
    override fun le(`var`: IVar, ub: IVar) {
        val constraint = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0.0)
        constraint.setCoefficient(`var`.getDelegate(), `var`.coeff)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
    }

    override fun eq(`var`: IVar, value: Double) {
        val constraint = solver.makeConstraint(value, value)
        constraint.setCoefficient(`var`.getDelegate(), `var`.coeff)
    }

    override fun eq(`var`: IVar, value: IVar) {
        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(`var`.getDelegate(), `var`.coeff)
        constraint.setCoefficient(value.getDelegate(), -value.coeff)
    }

    override fun between(`var`: IVar, lb: Double, ub: Double) {
        val constraint = solver.makeConstraint(lb, ub)
        constraint.setCoefficient(`var`.getDelegate(), `var`.coeff)
    }

    override fun between(`var`: IVar, lb: IVar, ub: IVar) {
        ge(`var`, lb)
        le(`var`, ub)
    }

    /**
     * lb <= dividend/divisor <= ub
     * `var`2*lb <= `var`1
     * `var`1    <= `var`2*ub
     */
    override fun between(dividend: IVar, divisor: IVar, lb: Double, ub: Double) {
        le(divisor.coeff(lb), dividend)
        ge(divisor.coeff(ub), dividend)
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
    override fun geIf(`var`: IVar, value: Double, bool: IVar) {
        ge(arrayOf(`var`, bool.coeff(-(value - dlb))), dlb)
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
    override fun geIfNot(`var`: IVar, value: Double, bool: IVar) {
        ge(arrayOf(`var`, bool.coeff((value - dlb))), value)
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
    override fun leIf(`var`: IVar, value: Double, bool: IVar) {
        le(arrayOf(`var`, bool.coeff(-(value - dub))), dub)
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
    override fun leIfNot(`var`: IVar, value: Double, bool: IVar) {
        le(arrayOf(`var`, bool.coeff((value - dub))), value)
    }

    /**
     * <pre>
     * bool为1时：var == value
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun eqIf(`var`: IVar, value: Double, bool: IVar) {
        geIf(`var`, value, bool)
        leIf(`var`, value, bool)
    }

    /**
     * <pre>
     * bool为0时：var == value
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun eqIfNot(`var`: IVar, value: Double, bool: IVar) {
        geIfNot(`var`, value, bool)
        leIfNot(`var`, value, bool)
    }

    /**
     * <pre>
     * bool为1时：lb <= var <= ub
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun betweenIf(`var`: IVar, lb: Double, ub: Double, bool: IVar) {
        geIf(`var`, lb, bool)
        leIf(`var`, ub, bool)
    }


    /**
     * <pre>
     * bool为0时：lb <= var <= ub
     *
     * bool为1时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun betweenIfNot(`var`: IVar, lb: Double, ub: Double, bool: IVar) {
        geIfNot(`var`, lb, bool)
        leIfNot(`var`, ub, bool)
    }

    override fun sum(vars: Array<IVar>): IVar {
        val numVariables = numVariables()
        val sum = solver.makeNumVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, "n" + (numVariables + 1))

        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(sum, -1.0)
        for (i in vars.indices) {
            val v = vars[i]
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
        return MPVar(sum)
    }

    override fun minimize(vars: Array<IVar>): IVar {
        val objective = solver.objective()
        for (i in vars.indices) {
            val v = vars[i]
            objective.setCoefficient(v.getDelegate(), v.coeff)
        }

        objective.setMinimization()
        return MPObjectiveVar(objective)
    }

    override fun maximize(vars: Array<IVar>): IVar {
        val objective = solver.objective()
        for (i in vars.indices) {
            val v = vars[i]
            objective.setCoefficient(v.getDelegate(), v.coeff)
        }

        objective.setMaximization()
        return MPObjectiveVar(objective)
    }

    override fun atMostOne(vars: Array<IVar>) {
        atMost(vars, 1)
    }

    /**
     * 最多n个非零变量
     */
    override fun atMost(vars: Array<IVar>, n: Int) {
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

    override fun setTimeLimit(seconds: Int) {
        solver.setTimeLimit(seconds * 1000L)
    }

    override fun solve() {
        resultStatus = solver.solve()
    }

    override fun isOptimal(): Boolean {
        return MPSolver.ResultStatus.OPTIMAL == resultStatus
    }

    override fun getResultStatus(): String {
        return resultStatus.name
    }

    override fun numVariables(): Int {
        return solver.numVariables()
    }

    override fun numConstraints(): Int {
        return solver.numConstraints()
    }
}
