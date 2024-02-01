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
            val a = numVar(0.0, 10.0)
            val b = numVar(0.0, 10.0)
            //if a >0 ,b <=0
            a.leIfNot(0.0, boolVar)
            b.leIf(0.0,boolVar)
            b.gt(0.0)
            arrayOf(a).maximize()
            solve()
            System.err.println(a.value)
            System.err.println(b.value)
            System.err.println(boolVar.value)
        }
    }

    @Test
    fun double() {
        System.err.println(0.0* Double.MAX_VALUE)
    }
}