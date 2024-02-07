package top.bettercode.summer.tools.optimal.solver

object SolverFactory {

    @JvmStatic
    @JvmOverloads
    fun createSolver(
            solverType: SolverType,
            epsilon: Double = OptimalUtil.DEFAULT_EPSILON,
            logging: Boolean = false,
    ): Solver {
        return when (solverType) {
            SolverType.COPT -> COPTSolver(epsilon = epsilon, logging = logging)
            SolverType.CPLEX -> CplexSolver(epsilon = epsilon, logging = logging)
            SolverType.GUROBI -> CplexSolver(epsilon = epsilon, logging = logging)
            SolverType.SCIP -> SCIPSolver(epsilon = epsilon)
            SolverType.CBC -> CBCSolver(epsilon = epsilon)
        }
    }

}
