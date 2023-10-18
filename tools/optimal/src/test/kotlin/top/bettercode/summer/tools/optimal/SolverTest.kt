package top.bettercode.summer.tools.optimal

import top.bettercode.summer.tools.optimal.solver.COPTSolver
import top.bettercode.summer.tools.optimal.solver.Solver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class SolverTest {

    //    private val solver: Solver = MPExtSolver()
    private val solver: Solver = COPTSolver()

    @Test
    fun numVariables() {
        val count = 2000
        val numVarArray = solver.numVarArray(count, 0.0, 1000.0)

//        solver.boolVar()
        val minimize = solver.minimize(numVarArray)
        System.err.println("变量数量：" + solver.numVariables() + " 约束数量：" + solver.numConstraints())
        solver.solve()
        System.err.println(minimize.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun geIf() {
        val boolVar = solver.boolVar()
        solver.eq(boolVar, 1.0)
        val numVar = solver.numVar(0.0, 1000.0)
        solver.geIf(numVar, 100.0, boolVar)
//        solver.eq(numVar, 101.0)
//        solver.minimize(arrayOf(numVar))
        solver.maximize(arrayOf(numVar))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun geIfNot() {
        val boolVar = solver.boolVar()
//        solver.eq(boolVar, 0.0)
        val numVar = solver.numVar(0.0, 1000.0)
        solver.geIfNot(numVar, 100.0, boolVar)
        solver.minimize(arrayOf(numVar))
//        solver.maximize(arrayOf(numVar))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun geIf2() {
        val boolVar = solver.boolVar()
        solver.eq(boolVar, 1.0)
        val numVar = solver.numVar(0.0, 1000.0)
        //     * dlb <= x - (value-dlb)*bool
        solver.geIf(numVar, 100.0, boolVar)
//             * x + (value-dub)*bool <=  value
        solver.leIfNot(numVar, 100.0 - solver.epsilon, boolVar)
//        solver.eq(numVar, 99.0)
        solver.minimize(arrayOf(numVar))
//        solver.maximize(arrayOf(numVar))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun le() {
        val boolVar = solver.boolVar()
//        solver.eq(boolVar, 1.0)
        val numVar = solver.numVar(0.0, 1000.0)
        solver.leIf(numVar, 100.0, boolVar)
//        solver.minimize(arrayOf(numVar))
        solver.maximize(arrayOf(numVar))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun betweenIf() {
        val boolVar = solver.boolVar()
//        solver.eq(boolVar, 1.0)
        val numVar = solver.numVar(0.0, 1000.0)
        solver.betweenIf(numVar, 100.0, 200.0, boolVar)
//        solver.eq(numVar, 150.0)
        solver.minimize(arrayOf(numVar))
//        solver.maximize(arrayOf(numVar))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun eqIf() {
        val boolVar = solver.boolVar()
        solver.eq(boolVar, 1.0)
        val numVar = solver.numVar(0.0, 1000.0)
        solver.eqIf(numVar, 100.0, boolVar)
//        solver.eq(numVar, 150.0)
        solver.minimize(arrayOf(numVar))
//        solver.maximize(arrayOf(numVar))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun betweenIf2() {
        val boolVar = solver.boolVar()
//        solver.eq(boolVar, 1.0)
        val numVar = solver.numVar(0.0, 1000.0)
        solver.betweenIf(numVar, 100.0, 200.0, boolVar)
        solver.geIfNot(numVar, 200.0 + solver.epsilon, boolVar)
//        solver.leIfNot(numVar, 100.0-solver.epsilon, boolVar)
//        solver.eq(numVar, 150.0)
        solver.minimize(arrayOf(numVar))
//        solver.maximize(arrayOf(numVar))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun betweenIfNot() {
        val boolVar = solver.boolVar()
        solver.eq(boolVar, 0.0)
        val numVar = solver.numVar(0.0, 1000.0)
        solver.betweenIfNot(numVar, 100.0, 200.0, boolVar)
        solver.minimize(arrayOf(numVar))
//        solver.maximize(arrayOf(numVar))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    @Test
    fun atMost() {
        val numVarArray = solver.numVarArray(20, 0.0, 1000.0)
        solver.atMost(numVarArray, 5)
        solver.maximize(numVarArray)
        solver.solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertEquals(size, 5)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }
}