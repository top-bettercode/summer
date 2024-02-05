package top.bettercode.summer.tools.optimal.solver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class CplexSolverTest : COPTSolverTest() {
    override val solver: Solver = SolverFactory.createSolver(solverType = SolverType.CPLEX, logging = true)

    @Test
    override fun lpNumVariables() {
        solver.lpNumVariables(1000)
        Assertions.assertThrows(ilog.cplex.CpxException::class.java) {
            solver.lpNumVariables(1001)
        }
    }

    @Test
    override fun numVariables() {
        solver.numVariables(1000)
        Assertions.assertThrows(ilog.cplex.CpxException::class.java) {
            solver.numVariables(1001)
        }
    }
}