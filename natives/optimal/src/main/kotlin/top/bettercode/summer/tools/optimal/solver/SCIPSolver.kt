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
        dlb: Double = DEFAULT_LB,

        /**
         * 变量默认上界
         */
        dub: Double = DEFAULT_UB,
        epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
        name: String = "SCIPSolver"
) : MPExtSolver(type = MPSolver.OptimizationProblemType.SCIP_MIXED_INTEGER_PROGRAMMING, dlb = dlb, dub = dub, epsilon = epsilon, name = name)