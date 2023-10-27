package top.bettercode.summer.tools.optimal.solver

import com.google.ortools.linearsolver.MPSolver

/**
 *
 * @author Peter Wu
 */
class SCIPSolver @JvmOverloads constructor(
        /**
         * 变量默认下界
         */
        dlb: Double = 0.0,

        /**
         * 变量默认上界
         */
        dub: Double = 1000.0,
        epsilon: Double = 1e-6,
) : MPExtSolver(type = MPSolver.OptimizationProblemType.SCIP_MIXED_INTEGER_PROGRAMMING, dlb = dlb, dub = dub, epsilon = epsilon, name = "SCIPSolver")