package top.bettercode.summer.tools.optimal.ortools

import com.google.ortools.linearsolver.MPSolver
import top.bettercode.summer.tools.optimal.OptimalUtil
import top.bettercode.summer.tools.optimal.SolverType

/**
 * support min epsilon = 1e-4
 *
 * @author Peter Wu
 */
class CBCSolver @JvmOverloads constructor(
    epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
    minEpsilon: Double = 1e-3,
    name: String = "CBCSolver"
) : MPExtSolver(
    mpType = MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING,
    solverType = SolverType.CBC,
    epsilon = epsilon,
    minEpsilon = minEpsilon,
    name = name
)