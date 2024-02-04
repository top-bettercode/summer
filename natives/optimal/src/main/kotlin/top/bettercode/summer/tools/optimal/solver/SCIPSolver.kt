package top.bettercode.summer.tools.optimal.solver

import com.google.ortools.linearsolver.MPSolver

/**
 *
 * @author Peter Wu
 */
class SCIPSolver @JvmOverloads constructor(
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "SCIPSolver"
) : MPExtSolver(type = MPSolver.OptimizationProblemType.SCIP_MIXED_INTEGER_PROGRAMMING, epsilon = epsilon, name = name)