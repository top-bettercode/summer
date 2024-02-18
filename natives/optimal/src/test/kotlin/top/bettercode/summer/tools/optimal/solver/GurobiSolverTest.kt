package top.bettercode.summer.tools.optimal.solver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class GurobiSolverTest : COPTSolverTest() {
    override val solver: Solver = SolverFactory.createSolver(solverType = SolverType.GUROBI, logging = true, epsilon = 1e-4)

    @Test
    override fun lpNumVariables() {
        solver.use {
            it.lpNumVariables(2000)
            Assertions.assertThrows(com.gurobi.gurobi.GRBException::class.java) {
                it.lpNumVariables(2001)
            }
        }
    }

    @Test
    override fun numVariables() {
        solver.use {
            it.numVariables(2000)
            Assertions.assertThrows(com.gurobi.gurobi.GRBException::class.java) {
                it.numVariables(2001)
            }
        }
    }

}