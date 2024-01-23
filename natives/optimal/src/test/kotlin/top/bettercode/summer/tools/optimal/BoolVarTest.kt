package top.bettercode.summer.tools.optimal

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.optimal.solver.SolverFactory
import top.bettercode.summer.tools.optimal.solver.SolverType

/**
 *
 * @author Peter Wu
 */
class BoolVarTest {
    private val coptSolver: Solver = SolverFactory.createSolver(solverType = SolverType.COPT, logging = true)
    private val cbcSolver: Solver = SolverFactory.createSolver(solverType = SolverType.CBC)
    private val scipSolver: Solver = SolverFactory.createSolver(solverType = SolverType.SCIP)

    @Test
    fun test() {
        scipSolver.apply {
            val boolVar = boolVar()
            val numVar = numVar(0.0, 10.0)
            numVar.eqIf(1.0, boolVar)
            arrayOf(numVar).minimize()
            boolVar.eq(1.0)
            solve()
            System.err.println(numVar.value)
            System.err.println(boolVar.value)
        }
    }
}