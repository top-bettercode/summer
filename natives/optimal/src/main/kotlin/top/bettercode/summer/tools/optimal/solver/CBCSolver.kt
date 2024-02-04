package top.bettercode.summer.tools.optimal.solver

import com.google.ortools.linearsolver.MPSolver

/**
 *
 * @author Peter Wu
 */
class CBCSolver @JvmOverloads constructor(
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "CBCSolver"
) : MPExtSolver(type = MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING, epsilon = epsilon, name = name)