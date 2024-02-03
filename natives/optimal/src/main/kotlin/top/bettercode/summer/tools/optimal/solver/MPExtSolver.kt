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
        /**
         * 变量默认下界
         */
        val dlb: Double = DEFAULT_LB,

        /**
         * 变量默认上界
         */
        val dub: Double = DEFAULT_UB,
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "MPSolver"
) : Solver(name, epsilon) {

    companion object {
        /**
         * 默认下限
         */
        @JvmStatic
        var DEFAULT_LB: Double = 0.0

        /**
         * 默认上限
         */
        @JvmStatic
        var DEFAULT_UB: Double = 1000.0

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
        val constraint = solver.makeConstraint(lb, MPSolver.infinity())
        constraint.setCoefficient(this.getDelegate(), this.coeff)
    }

    /**
     * this>=lb
     * this-lb>=0
     */
    override fun IVar.ge(lb: IVar) {
        val constraint = solver.makeConstraint(0.0, MPSolver.infinity())
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
    }

    override fun Array<out IVar>.ge(lb: Double) {
        val constraint = solver.makeConstraint(lb, MPSolver.infinity())
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.ge(lb: Double) {
        val constraint = solver.makeConstraint(lb, MPSolver.infinity())
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    /**
     * this>=lb
     * this-lb>=0
     */
    override fun Array<out IVar>.ge(lb: IVar) {
        val constraint = solver.makeConstraint(0.0, MPSolver.infinity())
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.ge(lb: IVar) {
        val constraint = solver.makeConstraint(0.0, MPSolver.infinity())
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun IVar.gt(lb: IVar) {
        val constraint = solver.makeConstraint(epsilon, MPSolver.infinity())
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
    }

    override fun Array<out IVar>.gt(lb: IVar) {
        val constraint = solver.makeConstraint(epsilon, MPSolver.infinity())
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.gt(lb: IVar) {
        val constraint = solver.makeConstraint(epsilon, MPSolver.infinity())
        constraint.setCoefficient(lb.getDelegate(), -lb.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }


    override fun IVar.le(ub: Double) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), ub)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
    }

    /**
     * this<=ub
     */
    override fun IVar.le(ub: IVar) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), 0.0)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
    }

    override fun Array<out IVar>.le(ub: Double) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), ub)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.le(ub: Double) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), ub)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    /**
     *  this<=ub
     *  this-ub<=0
     */
    override fun Array<out IVar>.le(ub: IVar) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), 0.0)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.le(ub: IVar) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), 0.0)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun IVar.lt(ub: IVar) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), -epsilon)
        constraint.setCoefficient(this.getDelegate(), this.coeff)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
    }

    override fun Array<out IVar>.lt(ub: IVar) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), -epsilon)
        constraint.setCoefficient(ub.getDelegate(), -ub.coeff)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
    }

    override fun Iterable<IVar>.lt(ub: IVar) {
        val constraint = solver.makeConstraint(-MPSolver.infinity(), -epsilon)
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
        arrayOf(this, bool * (-(value - dlb))).ge(dlb)
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
        arrayOf(this, bool * (value - dlb)).ge(value)
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
        arrayOf(this, bool * -(value - dub)).le(dub)
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
        arrayOf(this, bool * (value - dub)).le(value)
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
        log.warn("MPSolver eqIf experimental")
        leIf(value, bool)
        geIf(value, bool)
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
        log.warn("MPSolver eqIf experimental")
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
        val numVariables = numVariables()
        val sum = solver.makeNumVar(-MPSolver.infinity(), MPSolver.infinity(), "n" + (numVariables + 1))

        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(sum, -1.0)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
        return MPVar(sum)
    }

    override fun Iterable<IVar>.sum(): IVar {
        val numVariables = numVariables()
        val sum = solver.makeNumVar(-MPSolver.infinity(), MPSolver.infinity(), "n" + (numVariables + 1))

        val constraint = solver.makeConstraint(0.0, 0.0)
        constraint.setCoefficient(sum, -1.0)
        for (v in this) {
            constraint.setCoefficient(v.getDelegate(), v.coeff)
        }
        return MPVar(sum)
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
