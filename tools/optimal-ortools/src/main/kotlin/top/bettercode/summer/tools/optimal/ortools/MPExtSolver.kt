package top.bettercode.summer.tools.optimal.ortools

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPSolverParameters
import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.optimal.OptimalUtil
import top.bettercode.summer.tools.optimal.OptimalUtil.isInt
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.optimal.SolverType
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * @author Peter Wu
 */
open class MPExtSolver @JvmOverloads constructor(
    /**
     * OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING
     * OptimizationProblemType.SCIP_MIXED_INTEGER_PROGRAMMING
     */
    private val mpType: MPSolver.OptimizationProblemType,
    solverType: SolverType,
    epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
    minEpsilon: Double,
    name: String = "MPSolver",
) : Solver(
    name = name,
    type = solverType,
    epsilon = epsilon,
    minEpsilon = minEpsilon,
    communityLimits = Int.MAX_VALUE
) {

    companion object {

        init {
            Loader.loadNativeLibraries()
        }
    }

    val parameters = MPSolverParameters()
    var solver: MPSolver = MPSolver(name, mpType)

    var resultStatus: MPSolver.ResultStatus = MPSolver.ResultStatus.NOT_SOLVED

    init {
        parameters.setDoubleParam(MPSolverParameters.DoubleParam.RELATIVE_MIP_GAP, 1e-9)
        if (MPSolver.OptimizationProblemType.SCIP_MIXED_INTEGER_PROGRAMMING == mpType) {
            parameters.setDoubleParam(MPSolverParameters.DoubleParam.PRIMAL_TOLERANCE, 1e-9)
            parameters.setDoubleParam(MPSolverParameters.DoubleParam.DUAL_TOLERANCE, 1e-9)
        }
    }

    override fun setTimeLimit(seconds: Long) {
        solver.setTimeLimit(seconds * 1000L)
    }

    override fun read(filename: String) {
        throw UnsupportedOperationException("不支持")
    }

    override fun write(filename: String) {
        val lpFormat =
            if (filename.endsWith(".lp"))
                solver.exportModelAsLpFormat()
            else if (filename.endsWith(".mps"))
                solver.exportModelAsMpsFormat()
            else
                throw IllegalArgumentException("不支持的格式")
        File(filename).writeText(lpFormat)
    }

    override fun solve() {
        resultStatus = solver.solve(parameters)
    }

    override fun close() {
        solver.clear()
    }

    override fun reset() {
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
        return MPVar(
            _delegate = solver.makeBoolVar(
                name
                    ?: ("b" + (numVariables() + 1))
            ), isInt = true
        )
    }

    override fun intVar(lb: Double, ub: Double, name: String?): IVar {
        return MPVar(
            _delegate = solver.makeIntVar(
                lb, ub, name
                    ?: ("i" + (numVariables() + 1))
            ), isInt = true
        )
    }

    override fun numVar(lb: Double, ub: Double, name: String?): IVar {
        return MPVar(
            _delegate = solver.makeNumVar(
                lb, ub, name
                    ?: ("n" + (numVariables() + 1))
            ), isInt = false
        )
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


    override fun Array<out IVar>.sum(): IVar {
        val constraint = solver.makeConstraint(0.0, 0.0)
        var isInt = true
        var lb = 0.0
        var ub = 0.0
        for (it in this) {
            if (lb > -INFINITY)
                lb += it.lb
            if (ub < INFINITY)
                ub += it.ub
            if (!it.isInt || !it.coeff.isInt) {
                isInt = false
            }
            constraint.setCoefficient(it.getDelegate(), it.coeff)
        }
        lb = max(lb, -INFINITY)
        ub = min(ub, INFINITY)
        val sum = if (isInt) intVar(lb, ub) else numVar(lb, ub)
        constraint.setCoefficient(sum.getDelegate(), -1.0)
        return sum
    }

    override fun Iterable<IVar>.sum(): IVar {
        val constraint = solver.makeConstraint(0.0, 0.0)
        var isInt = true
        var lb = 0.0
        var ub = 0.0
        for (it in this) {
            if (lb > -INFINITY)
                lb += it.lb
            if (ub < INFINITY)
                ub += it.ub
            if (!it.isInt || !it.coeff.isInt) {
                isInt = false
            }
            constraint.setCoefficient(it.getDelegate(), it.coeff)
        }
        lb = max(lb, -INFINITY)
        ub = min(ub, INFINITY)
        val sum = if (isInt) intVar(lb, ub) else numVar(lb, ub)
        constraint.setCoefficient(sum.getDelegate(), -1.0)
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
