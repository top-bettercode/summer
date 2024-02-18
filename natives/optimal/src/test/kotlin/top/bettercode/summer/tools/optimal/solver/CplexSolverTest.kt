package top.bettercode.summer.tools.optimal.solver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class CplexSolverTest : COPTSolverTest() {
    override val solver: Solver = SolverFactory.createSolver(solverType = SolverType.CPLEX, logging = true, epsilon = 1e-6)

    @Test
    override fun lpNumVariables() {
        solver.use {
            it.lpNumVariables(1000)
            Assertions.assertThrows(ilog.cplex.CpxException::class.java) {
                it.lpNumVariables(1001)
            }
        }
    }

    @Test
    override fun numVariables() {
        solver.use {
            it.numVariables(1000)
            Assertions.assertThrows(ilog.cplex.CpxException::class.java) {
                it.numVariables(1001)
            }
        }
    }
}