package top.bettercode.summer.tools.optimal.solver

import top.bettercode.summer.tools.optimal.solver.OptimalUtil.DEFAULT_DUB

object SolverFactory {

    @JvmStatic
    @JvmOverloads
    fun createSolver(solverType: SolverType,
                     epsilon: Double = 1e-6,
                     logging: Boolean = false,
                     /**
                      * 变量默认下界
                      */
                     dlb: Double = 0.0,

                     /**
                      * 变量默认上界
                      */
                     dub: Double = DEFAULT_DUB
    ): Solver {
        return when (solverType) {
            SolverType.COPT -> COPTSolver(epsilon = epsilon, logging = logging)
            SolverType.SCIP -> SCIPSolver(dlb = dlb, dub = dub, epsilon = epsilon)
            SolverType.CBC -> CBCSolver(dlb = dlb, dub = dub, epsilon = epsilon)
        }
    }

}
