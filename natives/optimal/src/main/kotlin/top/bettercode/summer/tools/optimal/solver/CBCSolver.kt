package top.bettercode.summer.tools.optimal.solver

import com.google.ortools.linearsolver.MPSolver

/**
 *
 * @author Peter Wu
 */
class CBCSolver @JvmOverloads constructor(
        /**
         * 变量默认下界
         */
        dlb: Double = 0.0,

        /**
         * 变量默认上界
         */
        dub: Double = OptimalUtil.DEFAULT_DUB,
        epsilon: Double = 1e-6,
        name: String = "CBCSolver"
) : MPExtSolver(type = MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING, dlb = dlb, dub = dub, epsilon = epsilon, name = name)