package top.bettercode.summer.tools.optimal.solver

import org.junit.jupiter.api.Disabled

/**
 *
 * @author Peter Wu
 */
@Disabled
class GurobiSolverTest : COPTSolverTest() {
    override val solver: Solver = SolverFactory.createSolver(solverType = SolverType.GUROBI, epsilon = 1e-5)


}