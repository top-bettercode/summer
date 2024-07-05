package top.bettercode.summer.tools.optimal.ortools

import com.google.ortools.linearsolver.MPSolver
import top.bettercode.summer.tools.optimal.OptimalUtil
import top.bettercode.summer.tools.optimal.SolverType

/**
 * support min epsilon = 1e-6
 *
 * @author Peter Wu
 */
class SCIPSolver @JvmOverloads constructor(
    epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
    minEpsilon: Double = 1e-6,
    name: String = "SCIPSolver"
) : MPExtSolver(
    mpType = MPSolver.OptimizationProblemType.SCIP_MIXED_INTEGER_PROGRAMMING,
    solverType = SolverType.SCIP,
    epsilon = epsilon,
    minEpsilon = minEpsilon,
    name = name
)