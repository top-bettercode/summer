package top.bettercode.summer.tools.optimal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.solver.CBCSolver
import top.bettercode.summer.tools.optimal.solver.COPTSolver
import top.bettercode.summer.tools.optimal.solver.SCIPSolver
import top.bettercode.summer.tools.optimal.solver.Solver

/**
 *
 * @author Peter Wu
 */
class SolverTest {

    private val cbcSolver: Solver = CBCSolver()
    private val scipSolver: Solver = SCIPSolver()
    private val coptSolver: Solver = COPTSolver(logging = true)

    @Test
    fun ge() {
        ge(solver = cbcSolver)
        ge(solver = scipSolver)
        ge(solver = coptSolver)
        ge(solver = cbcSolver, `var` = true)
        ge(solver = scipSolver, `var` = true)
        ge(solver = coptSolver, `var` = true)
        ge(solver = cbcSolver, array = true)
        ge(solver = scipSolver, array = true)
        ge(solver = coptSolver, array = true)
        ge(solver = cbcSolver, array = true, `var` = true)
        ge(solver = scipSolver, array = true, `var` = true)
        ge(solver = coptSolver, array = true, `var` = true)
    }

    fun ge(solver: Solver, array: Boolean = false, `var`: Boolean = false) {
        val numVar1 = solver.numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                solver.ge(arrayOf(numVar1), solver.numVar(10.0, 10.0))
            else
                solver.ge(arrayOf(numVar1), 10.0)
        } else {
            if (`var`)
                solver.ge(numVar1, solver.numVar(10.0, 10.0))
            else
                solver.ge(numVar1, 10.0)
        }
        solver.minimize(arrayOf(numVar1))
        solver.solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertEquals(10.0, numVar1.value)
    }

    @Test
    fun gt() {
        gt(solver = cbcSolver)
        gt(solver = scipSolver)
        gt(solver = coptSolver)
        gt(solver = cbcSolver, `var` = true)
        gt(solver = scipSolver, `var` = true)
        gt(solver = coptSolver, `var` = true)
        gt(solver = cbcSolver, array = true)
        gt(solver = scipSolver, array = true)
        gt(solver = coptSolver, array = true)
        gt(solver = cbcSolver, array = true, `var` = true)
        gt(solver = scipSolver, array = true, `var` = true)
        gt(solver = coptSolver, array = true, `var` = true)
    }

    fun gt(solver: Solver, array: Boolean = false, `var`: Boolean = false) {
        val numVar1 = solver.numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                solver.gt(arrayOf(numVar1), solver.numVar(10.0, 10.0))
            else
                solver.gt(arrayOf(numVar1), 10.0)
        } else {
            if (`var`)
                solver.gt(numVar1, solver.numVar(10.0, 10.0))
            else
                solver.gt(numVar1, 10.0)
        }
        solver.minimize(arrayOf(numVar1))
        solver.solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value > 10.0)
    }

    @Test
    fun le() {
        le(solver = cbcSolver)
        le(solver = scipSolver)
        le(solver = coptSolver)
        le(solver = cbcSolver, `var` = true)
        le(solver = scipSolver, `var` = true)
        le(solver = coptSolver, `var` = true)
        le(solver = cbcSolver, array = true)
        le(solver = scipSolver, array = true)
        le(solver = coptSolver, array = true)
        le(solver = cbcSolver, array = true, `var` = true)
        le(solver = scipSolver, array = true, `var` = true)
        le(solver = coptSolver, array = true, `var` = true)
    }


    fun le(solver: Solver, array: Boolean = false, `var`: Boolean = false) {
        val numVar1 = solver.numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                solver.le(arrayOf(numVar1), solver.numVar(10.0, 10.0))
            else
                solver.le(arrayOf(numVar1), 10.0)
        } else {
            if (`var`)
                solver.le(numVar1, solver.numVar(10.0, 10.0))
            else
                solver.le(numVar1, 10.0)
        }
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertEquals(10.0, numVar1.value)
    }

    @Test
    fun lt() {
        lt(solver = cbcSolver)
        lt(solver = scipSolver)
        lt(solver = coptSolver)
        lt(solver = cbcSolver, `var` = true)
        lt(solver = scipSolver, `var` = true)
        lt(solver = coptSolver, `var` = true)
        lt(solver = cbcSolver, array = true)
        lt(solver = scipSolver, array = true)
        lt(solver = coptSolver, array = true)
        lt(solver = cbcSolver, array = true, `var` = true)
        lt(solver = scipSolver, array = true, `var` = true)
        lt(solver = coptSolver, array = true, `var` = true)
    }

    fun lt(solver: Solver, array: Boolean = false, `var`: Boolean = false) {
        val numVar1 = solver.numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                solver.lt(arrayOf(numVar1), solver.numVar(10.0, 10.0))
            else
                solver.lt(arrayOf(numVar1), 10.0)
        } else {
            if (`var`)
                solver.lt(numVar1, solver.numVar(10.0, 10.0))
            else
                solver.lt(numVar1, 10.0)
        }
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value < 10.0)
    }

    @Test
    fun eq() {
        eq(solver = cbcSolver)
        eq(solver = scipSolver)
        eq(solver = coptSolver)
        eq(solver = cbcSolver, `var` = true)
        eq(solver = scipSolver, `var` = true)
        eq(solver = coptSolver, `var` = true)
        eq(solver = cbcSolver, array = true)
        eq(solver = scipSolver, array = true)
        eq(solver = coptSolver, array = true)
        eq(solver = cbcSolver, array = true, `var` = true)
        eq(solver = scipSolver, array = true, `var` = true)
        eq(solver = coptSolver, array = true, `var` = true)
    }


    fun eq(solver: Solver, array: Boolean = false, `var`: Boolean = false) {
        val numVar1 = solver.numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                solver.eq(arrayOf(numVar1), solver.numVar(10.0, 10.0))
            else
                solver.eq(arrayOf(numVar1), 10.0)
        } else {
            if (`var`)
                solver.eq(numVar1, solver.numVar(10.0, 10.0))
            else
                solver.eq(numVar1, 10.0)
        }
        solver.minimize(arrayOf(numVar1))
        solver.solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertEquals(10.0, numVar1.value)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertEquals(10.0, numVar1.value)
    }

    @Test
    fun between() {
        between(solver = cbcSolver)
        between(solver = scipSolver)
        between(solver = coptSolver)
        between(solver = cbcSolver, `var` = true)
        between(solver = scipSolver, `var` = true)
        between(solver = coptSolver, `var` = true)
        between(solver = cbcSolver, array = true)
        between(solver = scipSolver, array = true)
        between(solver = coptSolver, array = true)
        between(solver = cbcSolver, array = true, `var` = true)
        between(solver = scipSolver, array = true, `var` = true)
        between(solver = coptSolver, array = true, `var` = true)
    }

    fun between(solver: Solver, array: Boolean = false, `var`: Boolean = false) {
        val numVar1 = solver.numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                solver.between(arrayOf(numVar1), solver.numVar(10.0, 10.0), solver.numVar(20.0, 20.0))
            else
                solver.between(arrayOf(numVar1), 10.0, 20.0)
        } else {
            if (`var`)
                solver.between(numVar1, solver.numVar(10.0, 10.0), solver.numVar(20.0, 20.0))
            else
                solver.between(numVar1, 10.0, 20.0)
        }
        solver.minimize(arrayOf(numVar1))
        solver.solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value >= 10.0)
        Assertions.assertTrue(numVar1.value <= 20.0)
        Assertions.assertTrue(numVar1.value == 10.0)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value >= 10.0)
        Assertions.assertTrue(numVar1.value <= 20.0)
        Assertions.assertTrue(numVar1.value == 20.0)
    }

    @Test
    fun geIf() {
        geIf(solver = cbcSolver)
        geIf(solver = scipSolver)
        geIf(solver = coptSolver)
    }

    fun geIf(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.geIf(numVar1, 10.0, boolVar)
        solver.minimize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.minimize(arrayOf(numVar1))
        solver.eq(boolVar, 1.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value >= 10.0)
    }

    @Test
    fun geIfNot() {
        geIfNot(solver = cbcSolver)
        geIfNot(solver = scipSolver)
        geIfNot(solver = coptSolver)
    }

    fun geIfNot(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.geIfNot(numVar1, 10.0, boolVar)
        solver.minimize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.minimize(arrayOf(numVar1))
        solver.eq(boolVar, 0.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value >= 10.0)
    }

    @Test
    fun gtIf() {
        gtIf(solver = cbcSolver)
        gtIf(solver = scipSolver)
        gtIf(solver = coptSolver)
    }


    fun gtIf(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.gtIf(numVar1, 10.0, boolVar)
        solver.minimize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.minimize(arrayOf(numVar1))
        solver.eq(boolVar, 1.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value > 10.0)
    }

    @Test
    fun gtIfNot() {
        gtIfNot(solver = cbcSolver)
        gtIfNot(solver = scipSolver)
        gtIfNot(solver = coptSolver)
    }

    fun gtIfNot(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.gtIfNot(numVar1, 10.0, boolVar)
        solver.minimize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.minimize(arrayOf(numVar1))
        solver.eq(boolVar, 0.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value > 10.0)
    }

    @Test
    fun leIf() {
        leIf(solver = coptSolver)
        leIf(solver = scipSolver)
        leIf(solver = cbcSolver)
    }


    fun leIf(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.leIf(numVar1, 10.0, boolVar)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.maximize(arrayOf(numVar1))
        solver.eq(boolVar, 1.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value <= 10.0)
    }

    @Test
    fun leIfNot() {
        leIfNot(solver = coptSolver)
        leIfNot(solver = scipSolver)
        leIfNot(solver = cbcSolver)
    }

    fun leIfNot(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.leIfNot(numVar1, 10.0, boolVar)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.maximize(arrayOf(numVar1))
        solver.eq(boolVar, 0.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value <= 10.0)
    }

    @Test
    fun ltIf() {
        ltIf(solver = coptSolver)
        ltIf(solver = scipSolver)
        ltIf(solver = cbcSolver)
    }


    fun ltIf(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.ltIf(numVar1, 10.0, boolVar)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.maximize(arrayOf(numVar1))
        solver.eq(boolVar, 1.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value < 10.0)
    }

    @Test
    fun ltIfNot() {
        ltIfNot(solver = coptSolver)
        ltIfNot(solver = scipSolver)
        ltIfNot(solver = cbcSolver)
    }

    fun ltIfNot(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.ltIfNot(numVar1, 10.0, boolVar)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.maximize(arrayOf(numVar1))
        solver.eq(boolVar, 0.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value < 10.0)
    }

    @Test
    fun eqIf() {
        eqIf(solver = coptSolver)
        eqIf(solver = scipSolver)
        eqIf(solver = cbcSolver)
    }

    fun eqIf(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.eqIf(numVar1, 10.0, boolVar)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.maximize(arrayOf(numVar1))
        solver.eq(boolVar, 1.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value == 10.0)
    }

    @Test
    fun eqIfNot() {
        eqIfNot(solver = coptSolver)
        eqIfNot(solver = scipSolver)
        eqIfNot(solver = cbcSolver)
    }

    fun eqIfNot(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.eqIfNot(numVar1, 10.0, boolVar)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.maximize(arrayOf(numVar1))
        solver.eq(boolVar, 0.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value == 10.0)
    }

    @Test
    fun betweenIf() {
        betweenIf(solver = coptSolver)
        betweenIf(solver = scipSolver)
        betweenIf(solver = cbcSolver)
    }

    fun betweenIf(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.betweenIf(numVar1, 10.0, 20.0, boolVar)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.minimize(arrayOf(numVar1))
        solver.eq(boolVar, 1.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value in 10.0..20.0)
        Assertions.assertTrue(numVar1.value == 10.0)
        solver.maximize(arrayOf(numVar1))
        solver.eq(boolVar, 1.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value in 10.0..20.0)
        Assertions.assertTrue(numVar1.value == 20.0)
    }

    @Test
    fun betweenIfNot() {
        betweenIfNot(solver = coptSolver)
        betweenIfNot(solver = scipSolver)
        betweenIfNot(solver = cbcSolver)
    }

    fun betweenIfNot(solver: Solver) {
        val numVar1 = solver.numVar(0.0, 100.0)
        val boolVar = solver.boolVar()
        solver.betweenIfNot(numVar1, 10.0, 20.0, boolVar)
        solver.maximize(arrayOf(numVar1))
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        solver.minimize(arrayOf(numVar1))
        solver.eq(boolVar, 0.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value in 10.0..20.0)
        Assertions.assertTrue(numVar1.value == 10.0)
        solver.maximize(arrayOf(numVar1))
        solver.eq(boolVar, 0.0)
        solver.solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(numVar1.value in 10.0..20.0)
        Assertions.assertTrue(numVar1.value == 20.0)
    }

    @Test
    fun atMostOne() {
        atMostOne(solver = cbcSolver)
        atMostOne(solver = scipSolver)
        atMostOne(solver = coptSolver)
    }

    fun atMostOne(solver: Solver) {
        val numVarArray = solver.numVarArray(20, 0.0, 1000.0)
        solver.atMostOne(numVarArray)
        solver.maximize(numVarArray)
        solver.solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertEquals(size, 1)
    }

    @Test
    fun atMost() {
        atMost(solver = cbcSolver)
        atMost(solver = scipSolver)
        atMost(solver = coptSolver)
    }

    fun atMost(solver: Solver) {
        val numVarArray = solver.numVarArray(20, 0.0, 1000.0)
        solver.atMost(numVarArray, 5)
        solver.maximize(numVarArray)
        solver.solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertEquals(size, 5)
    }

    @Test
    fun atLeastOne() {
        atLeastOne(solver = cbcSolver)
        atLeastOne(solver = scipSolver)
        atLeastOne(solver = coptSolver)
    }

    fun atLeastOne(solver: Solver) {
        val numVarArray = solver.numVarArray(20, 0.0, 1000.0)
        solver.atLeastOne(numVarArray)
        solver.maximize(numVarArray)
        solver.solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(size >= 1)
        Assertions.assertTrue(size <= 20)
    }

    @Test
    fun atLeast() {
        atLeast(solver = cbcSolver)
        atLeast(solver = scipSolver)
        atLeast(solver = coptSolver)
    }

    fun atLeast(solver: Solver) {
        val numVarArray = solver.numVarArray(20, 0.0, 1000.0)
        solver.atLeast(numVarArray, 5)
        solver.maximize(numVarArray)
        solver.solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
        Assertions.assertTrue(size >= 5)
        Assertions.assertTrue(size <= 20)
    }

    @Test
    fun lpNumVariables() {
        lpNumVariables(coptSolver, 10000)
        Assertions.assertThrows(copt.CoptException::class.java) {
            lpNumVariables(coptSolver, 10001)
        }
        lpNumVariables(cbcSolver, 10001)
        lpNumVariables(scipSolver, 10001)
    }

    @Test
    fun numVariables() {
        numVariables(coptSolver, 2000)
        Assertions.assertThrows(copt.CoptException::class.java) {
            numVariables(coptSolver, 2001)
        }
        numVariables(cbcSolver, 2001)
        numVariables(scipSolver, 2001)
    }

    fun numVariables(solver: Solver, num: Int) {
        solver.clear()
        val numVarArray = solver.numVarArray(num / 2, 0.0, 1000.0)
        val intVarArray = solver.intVarArray(num - num / 2, 0.0, 1000.0)
        for (i in 0 until num / 2) {
            solver.le(numVarArray[i], i.toDouble())
        }
        for (i in 0 until num - num / 2) {
            solver.le(intVarArray[i], i.toDouble())
        }
        val minimize = solver.minimize(numVarArray)
        System.err.println("变量数量：" + solver.numVariables() + " 约束数量：" + solver.numConstraints())
        Assertions.assertTrue(solver.numVariables() == num)
        Assertions.assertTrue(solver.numConstraints() == num)
        solver.solve()
        System.err.println(minimize.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

    fun lpNumVariables(solver: Solver, num: Int) {
        solver.clear()
        val numVarArray = solver.numVarArray(num, 0.0, 1000.0)
        for (i in 0 until num) {
            solver.le(numVarArray[i], i.toDouble())
        }
        val minimize = solver.minimize(numVarArray)
        System.err.println("变量数量：" + solver.numVariables() + " 约束数量：" + solver.numConstraints())
        Assertions.assertTrue(solver.numVariables() == num)
        Assertions.assertTrue(solver.numConstraints() == num)
        solver.solve()
        System.err.println(minimize.value)
        Assertions.assertTrue(solver.isOptimal(), "result:" + solver.getResultStatus())
    }

}