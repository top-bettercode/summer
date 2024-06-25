package top.bettercode.summer.tools.optimal.ortools

import com.google.ortools.linearsolver.MPSolver
import top.bettercode.summer.tools.optimal.OptimalUtil
import top.bettercode.summer.tools.optimal.SolverType

/**
 *
 * @author Peter Wu
 */
class CBCSolver @JvmOverloads constructor(
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "CBCSolver"
) : MPExtSolver(
        mpType = MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING,
        solverType = SolverType.CBC,
        epsilon = epsilon,
        name = name)