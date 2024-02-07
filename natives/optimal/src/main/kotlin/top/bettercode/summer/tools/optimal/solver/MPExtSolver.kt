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
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "MPSolver"
) : Solver(name, epsilon) {

    companion object {

        init {
            Loader.loadNativeLibraries()
        }
    }

    val solver: MPSolver = MPSolver(name, type)

    var resultStatus: MPSolver.ResultStatus = MPSolver.ResultStatus.NOT_SOLVED

    override fun setTimeLimit(seconds: Int) {
        solver.setTimeLimit(seconds * 1000L)
    }

    override fun solve() {
        resultStatus = solver.solve()
    }

    override fun close() {
        solver.clear()
    }

    override fun clear() {
        solver.clear()
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

    override fun boolVar(name: String?): IVar {
        return MPVar(solver.makeBoolVar(name ?: ("b" + (numVariables() + 1))))
    }

    override fun intVar(lb: Double, ub: Double, name: String?): IVar {
        return MPVar(solver.makeIntVar(lb, ub, name ?: ("i" + (numVariables() + 1))))
    }

    override fun numVar(lb: Double, ub: Double, name: String?): IVar {
        return MPVar(solver.makeNumVar(lb, ub, name ?: ("n" + (numVariables() + 1))))
    }

    override fun IVar.ge(lb: Double) {
        val constraint = solver.makeConstraint(lb, INFINITY)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
    }

    /**
     * this>=lb
     * this-lb>=0
     */
    override fun IVar.ge(lb: IVar) {
        val constraint = solver.makeConstraint(0.0, INFINITY)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
    }

    override fun Array<out IVar>.ge(lb: Double) {
        val constraint = solver.makeConstraint(lb, INFINITY)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.ge(lb: Double) {
        val constraint = solver.makeConstraint(lb, INFINITY)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    /**
     * this>=lb
     * this-lb>=0
     */
    override fun Array<out IVar>.ge(lb: IVar) {
        val constraint = solver.makeConstraint(0.0, INFINITY)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.ge(lb: IVar) {
        val constraint = solver.makeConstraint(0.0, INFINITY)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun IVar.gt(lb: IVar) {
        val constraint = solver.makeConstraint(epsilon, INFINITY)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
    }

    override fun Array<out IVar>.gt(lb: IVar) {
        val constraint = solver.makeConstraint(epsilon, INFINITY)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.gt(lb: IVar) {
        val constraint = solver.makeConstraint(epsilon, INFINITY)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }


    override fun IVar.le(ub: Double) {
        val constraint = solver.makeConstraint(-INFINITY, ub)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
    }

    /**
     * this<=ub
     */
    override fun IVar.le(ub: IVar) {
        val constraint = solver.makeConstraint(-INFINITY, 0.0)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
    }

    override fun Array<out IVar>.le(ub: Double) {
        val constraint = solver.makeConstraint(-INFINITY, ub)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.le(ub: Double) {
        val constraint = solver.makeConstraint(-INFINITY, ub)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    /**
     *  this<=ub
     *  this-ub<=0
     */
    override fun Array<out IVar>.le(ub: IVar) {
        val constraint = solver.makeConstraint(-INFINITY, 0.0)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.le(ub: IVar) {
        val constraint = solver.makeConstraint(-INFINITY, 0.0)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun IVar.lt(ub: IVar) {
        val constraint = solver.makeConstraint(-INFINITY, -epsilon)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
    }

    override fun Array<out IVar>.lt(ub: IVar) {
        val constraint = solver.makeConstraint(-INFINITY, -epsilon)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.lt(ub: IVar) {
        val constraint = solver.makeConstraint(-INFINITY, -epsilon)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun IVar.eq(value: Double) {
        val constraint = solver.makeConstraint(value, value)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
    }

    override fun IVar.eq(value: IVar) {
        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(value.getDelegate(), -value.coeff)
    }

    override fun Array<out IVar>.eq(value: Double) {
        val constraint = solver.makeConstraint(value, value)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.eq(value: Double) {
        val constraint = solver.makeConstraint(value, value)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Array<out IVar>.eq(value: IVar) {
        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(value.getDelegate(), -value.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.eq(value: IVar) {
        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(value.getDelegate(), -value.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun IVar.between(lb: Double, ub: Double) {
        val constraint = solver.makeConstraint(lb, ub)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
    }

    override fun IVar.between(lb: IVar, ub: IVar) {
        ge(lb)
        le(ub)
    }

    /**
     * 两数之间
     */
    override fun Array<out IVar>.between(lb: Double, ub: Double) {
        val constraint = solver.makeConstraint(lb, ub)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.between(lb: Double, ub: Double) {
        val constraint = solver.makeConstraint(lb, ub)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
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
        arrayOf(this, bool * (-(value - this.lb))).ge(this.lb)
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
        arrayOf(this, bool * (value - this.lb)).ge(value)
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
        arrayOf(this, bool * -(value - this.ub)).le(this.ub)
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
        arrayOf(this, bool * (value - this.ub)).le(value)
    }

    /**
     * <pre>
     * bool为1时：var == value
     *           value <= var <= value
     *           value*bool + dlb*(1-bool) <= var <= value*bool + dub*(1-bool)
     *           dlb <= var-value*bool + dlb*bool
     *                  var-value*bool + dub*bool <= dub
     *           dlb <= var - (value-dlb)*bool
     *                  var - (value-dub)*bool <=  dub
     *
     * bool为0时：dlb <= var <= dub
     *
     * </pre>
     */
    override fun IVar.eqIf(value: Double, bool: IVar) {
        log.warn("MPSolver eqIf experimental")
        val bool1 = boolVar()
        val bool2 = boolVar()
        arrayOf(bool1, bool2).sum().geIf(2.0, bool)
        geIf(value, bool1)
        leIf(value, bool2)
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
        log.warn("MPSolver eqIfNot experimental")
        val bool1 = boolVar()
        val bool2 = boolVar()
        arrayOf(bool1, bool2).sum().geIfNot(2.0, bool)
        geIf(value, bool1)
        leIf(value, bool2)
    }

    override fun IVar.neIf(value: Double, bool: IVar) {
        log.warn("MPSolver eqIf experimental")
        val bool1 = boolVar()
        val bool2 = boolVar()
        arrayOf(bool1, bool2).sum().geIf(1.0, bool)
        gtIf(value, bool1)
        ltIf(value, bool2)
    }

    override fun IVar.neIfNot(value: Double, bool: IVar) {
        log.warn("MPSolver neIfNot experimental")
        val bool1 = boolVar()
        val bool2 = boolVar()
        arrayOf(bool1, bool2).sum().geIfNot(1.0, bool)
        gtIf(value, bool1)
        ltIf(value, bool2)
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
        val bool1 = boolVar()
        val bool2 = boolVar()
        arrayOf(bool1, bool2).sum().geIf(2.0, bool)
        geIf(lb, bool1)
        leIf(ub, bool2)
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
        val bool1 = boolVar()
        val bool2 = boolVar()
        arrayOf(bool1, bool2).sum().geIfNot(2.0, bool)
        geIf(lb, bool1)
        leIf(ub, bool2)
    }

    override fun Array<out IVar>.sum(): IVar {
        val sum = numVar()
        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(sum.getDelegate(), -1.0)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
        return sum
    }

    override fun Iterable<IVar>.sum(): IVar {
        val sum = numVar()
        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(sum.getDelegate(), -1.0)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
        return sum
    }

    override fun Array<out IVar>.minimize(): IVar {
        val objective = solver.objective()
        for (v in this) {
            objective.setCoefficient(v.getDelegate(), v.coeff)
        }

        objective.setMinimization()
        return MPObjectiveVar(objective)
    }

    override fun Iterable<IVar>.minimize(): IVar {
        val objective = solver.objective()
        for (v in this) {
            objective.setCoefficient(v.getDelegate(), v.coeff)
        }

        objective.setMinimization()
        return MPObjectiveVar(objective)
    }

    override fun Array<out IVar>.maximize(): IVar {
        val objective = solver.objective()
        for (v in this) {
            objective.setCoefficient(v.getDelegate(), v.coeff)
        }

        objective.setMaximization()
        return MPObjectiveVar(objective)
    }

    override fun Iterable<IVar>.maximize(): IVar {
        val objective = solver.objective()
        for (v in this) {
            objective.setCoefficient(v.getDelegate(), v.coeff)
        }

        objective.setMaximization()
        return MPObjectiveVar(objective)
    }

}
