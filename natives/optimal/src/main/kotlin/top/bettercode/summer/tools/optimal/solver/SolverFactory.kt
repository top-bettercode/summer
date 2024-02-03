package top.bettercode.summer.tools.optimal.solver

import top.bettercode.summer.tools.optimal.solver.MPExtSolver.Companion.DEFAULT_LB
import top.bettercode.summer.tools.optimal.solver.MPExtSolver.Companion.DEFAULT_UB

object SolverFactory {

    @JvmStatic
    @JvmOverloads
    fun createSolver(solverType: SolverType,
                     epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
                     logging: Boolean = false,
                     /**
                      * 变量默认下界
                      */
                     dlb: Double = DEFAULT_LB,

                     /**
                      * 变量默认上界
                      */
                     dub: Double = DEFAULT_UB
    ): Solver {
        return when (solverType) {
            SolverType.COPT -> COPTSolver(epsilon = epsilon, logging = logging)
            SolverType.SCIP -> SCIPSolver(dlb = dlb, dub = dub, epsilon = epsilon)
            SolverType.CBC -> CBCSolver(dlb = dlb, dub = dub, epsilon = epsilon)
        }
    }

}
