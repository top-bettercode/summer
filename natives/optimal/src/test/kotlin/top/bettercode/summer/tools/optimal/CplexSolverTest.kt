package top.bettercode.summer.tools.optimal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.solver.CplexNativeLibLoader
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.optimal.solver.SolverFactory
import top.bettercode.summer.tools.optimal.solver.SolverType

/**
 *
 * @author Peter Wu
 */
class CplexSolverTest : COPTSolverTest() {
    override val solver: Solver = SolverFactory.createSolver(solverType = SolverType.CPLEX)

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