package top.bettercode.summer.tools.optimal.solver

import com.google.ortools.linearsolver.MPSolver
import top.bettercode.summer.tools.optimal.solver.MPExtSolver.Companion.DEFAULT_LB

/**
 *
 * @author Peter Wu
 */
class CBCSolver @JvmOverloads constructor(
        /**
         * 变量默认下界
         */
        dlb: Double = DEFAULT_LB,

        /**
         * 变量默认上界
         */
        dub: Double = DEFAULT_UB,
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "CBCSolver"
) : MPExtSolver(type = MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING, dlb = dlb, dub = dub, epsilon = epsilon, name = name)