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
        dub: Double = 1000.0) : MPExtSolver(type = MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING, name = "CBCSolver", dlb = dlb, dub = dub)