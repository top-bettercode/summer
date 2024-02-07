package top.bettercode.summer.tools.optimal.solver

import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class CBCSolverTest : COPTSolverTest() {
    override val solver: Solver = SolverFactory.createSolver(solverType = SolverType.SCIP, epsilon = 1e-5)

    @Test
    override fun lpNumVariables() {
        solver.use {
            it.lpNumVariables(10001)
        }
    }

    @Test
    override fun numVariables() {
        solver.use {
            it.numVariables(2001)
        }
    }
}