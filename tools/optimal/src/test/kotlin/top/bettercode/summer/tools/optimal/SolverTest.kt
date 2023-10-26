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
        val ge = ge(solver = cbcSolver)
        val ge1 = ge(solver = scipSolver)
        val ge2 = ge(solver = coptSolver)
        val ge3 = ge(solver = cbcSolver, `var` = true)
        val ge4 = ge(solver = scipSolver, `var` = true)
        val ge5 = ge(solver = coptSolver, `var` = true)
        val ge6 = ge(solver = cbcSolver, array = true)
        val ge7 = ge(solver = scipSolver, array = true)
        val ge8 = ge(solver = coptSolver, array = true)
        val ge9 = ge(solver = cbcSolver, array = true, `var` = true)
        val ge10 = ge(solver = scipSolver, array = true, `var` = true)
        val ge11 = ge(solver = coptSolver, array = true, `var` = true)
        Assertions.assertEquals(ge, ge1)
        Assertions.assertEquals(ge, ge2)
        Assertions.assertEquals(ge, ge3)
        Assertions.assertEquals(ge, ge4)
        Assertions.assertEquals(ge, ge5)
        Assertions.assertEquals(ge, ge6)
        Assertions.assertEquals(ge, ge7)
        Assertions.assertEquals(ge, ge8)
        Assertions.assertEquals(ge, ge9)
        Assertions.assertEquals(ge, ge10)
        Assertions.assertEquals(ge, ge11)
    }

    fun ge(solver: Solver, array: Boolean = false, `var`: Boolean = false): Double {
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
        return numVar1.value
    }

    @Test
    fun gt() {
        val gt = gt(solver = cbcSolver)
        val gt1 = gt(solver = scipSolver)
        val gt2 = gt(solver = coptSolver)
        val gt3 = gt(solver = cbcSolver, `var` = true)
        val gt4 = gt(solver = scipSolver, `var` = true)
        val gt6 = gt(solver = coptSolver, `var` = true)
        val gt5 = gt(solver = cbcSolver, array = true)
        val gt7 = gt(solver = scipSolver, array = true)
        val gt8 = gt(solver = coptSolver, array = true)
        val gt9 = gt(solver = cbcSolver, array = true, `var` = true)
        val gt10 = gt(solver = scipSolver, array = true, `var` = true)
        val gt11 = gt(solver = coptSolver, array = true, `var` = true)
        Assertions.assertEquals(gt, gt1)
        Assertions.assertEquals(gt, gt2)
        Assertions.assertEquals(gt, gt3)
        Assertions.assertEquals(gt, gt4)
        Assertions.assertEquals(gt, gt5)
        Assertions.assertEquals(gt, gt6)
        Assertions.assertEquals(gt, gt7)
        Assertions.assertEquals(gt, gt8)
        Assertions.assertEquals(gt, gt9)
        Assertions.assertEquals(gt, gt10)
        Assertions.assertEquals(gt, gt11)
    }

    fun gt(solver: Solver, array: Boolean = false, `var`: Boolean = false): Double {
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
        return numVar1.value
    }

    @Test
    fun le() {
        val le = le(solver = cbcSolver)
        val le1 = le(solver = scipSolver)
        val le2 = le(solver = coptSolver)
        val le3 = le(solver = cbcSolver, `var` = true)
        val le4 = le(solver = scipSolver, `var` = true)
        val le5 = le(solver = coptSolver, `var` = true)
        val le6 = le(solver = cbcSolver, array = true)
        val le7 = le(solver = scipSolver, array = true)
        val le8 = le(solver = coptSolver, array = true)
        val le9 = le(solver = cbcSolver, array = true, `var` = true)
        val le10 = le(solver = scipSolver, array = true, `var` = true)
        val le11 = le(solver = coptSolver, array = true, `var` = true)
        Assertions.assertEquals(le, le1)
        Assertions.assertEquals(le, le2)
        Assertions.assertEquals(le, le3)
        Assertions.assertEquals(le, le4)
        Assertions.assertEquals(le, le5)
        Assertions.assertEquals(le, le6)
        Assertions.assertEquals(le, le7)
        Assertions.assertEquals(le, le8)
        Assertions.assertEquals(le, le9)
        Assertions.assertEquals(le, le10)
        Assertions.assertEquals(le, le11)
    }


    fun le(solver: Solver, array: Boolean = false, `var`: Boolean = false): Double {
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
        return numVar1.value
    }

    @Test
    fun lt() {
        val lt = lt(solver = cbcSolver)
        val lt1 = lt(solver = scipSolver)
        val lt2 = lt(solver = coptSolver)
        val lt3 = lt(solver = cbcSolver, `var` = true)
        val lt4 = lt(solver = scipSolver, `var` = true)
        val lt5 = lt(solver = coptSolver, `var` = true)
        val lt6 = lt(solver = cbcSolver, array = true)
        val lt7 = lt(solver = scipSolver, array = true)
        val lt8 = lt(solver = coptSolver, array = true)
        val lt9 = lt(solver = cbcSolver, array = true, `var` = true)
        val lt10 = lt(solver = scipSolver, array = true, `var` = true)
        val lt11 = lt(solver = coptSolver, array = true, `var` = true)
        Assertions.assertEquals(lt, lt1)
        Assertions.assertEquals(lt, lt2)
        Assertions.assertEquals(lt, lt3)
        Assertions.assertEquals(lt, lt4)
        Assertions.assertEquals(lt, lt5)
        Assertions.assertEquals(lt, lt6)
        Assertions.assertEquals(lt, lt7)
        Assertions.assertEquals(lt, lt8)
        Assertions.assertEquals(lt, lt9)
        Assertions.assertEquals(lt, lt10)
        Assertions.assertEquals(lt, lt11)
    }

    fun lt(solver: Solver, array: Boolean = false, `var`: Boolean = false): Double {
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
        return numVar1.value
    }

    @Test
    fun eq() {
        val eq = eq(solver = cbcSolver)
        val eq1 = eq(solver = scipSolver)
        val eq2 = eq(solver = coptSolver)
        val eq3 = eq(solver = cbcSolver, `var` = true)
        val eq4 = eq(solver = scipSolver, `var` = true)
        val eq5 = eq(solver = coptSolver, `var` = true)
        val eq6 = eq(solver = cbcSolver, array = true)
        val eq7 = eq(solver = scipSolver, array = true)
        val eq8 = eq(solver = coptSolver, array = true)
        val eq9 = eq(solver = cbcSolver, array = true, `var` = true)
        val eq10 = eq(solver = scipSolver, array = true, `var` = true)
        val eq11 = eq(solver = coptSolver, array = true, `var` = true)
        Assertions.assertEquals(eq, eq1)
        Assertions.assertEquals(eq, eq2)
        Assertions.assertEquals(eq, eq3)
        Assertions.assertEquals(eq, eq4)
        Assertions.assertEquals(eq, eq5)
        Assertions.assertEquals(eq, eq6)
        Assertions.assertEquals(eq, eq7)
        Assertions.assertEquals(eq, eq8)
        Assertions.assertEquals(eq, eq9)
        Assertions.assertEquals(eq, eq10)
        Assertions.assertEquals(eq, eq11)
    }


    fun eq(solver: Solver, array: Boolean = false, `var`: Boolean = false): Double {
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
        return numVar1.value
    }

    @Test
    fun between() {
        val bt = between(solver = cbcSolver)
        val bt1 = between(solver = scipSolver)
        val bt2 = between(solver = coptSolver)
        val bt3 = between(solver = cbcSolver, `var` = true)
        val bt4 = between(solver = scipSolver, `var` = true)
        val bt5 = between(solver = coptSolver, `var` = true)
        val bt6 = between(solver = cbcSolver, array = true)
        val bt7 = between(solver = scipSolver, array = true)
        val bt8 = between(solver = coptSolver, array = true)
        val bt9 = between(solver = cbcSolver, array = true, `var` = true)
        val bt10 = between(solver = scipSolver, array = true, `var` = true)
        val bt11 = between(solver = coptSolver, array = true, `var` = true)
        Assertions.assertEquals(bt, bt1)
        Assertions.assertEquals(bt, bt2)
        Assertions.assertEquals(bt, bt3)
        Assertions.assertEquals(bt, bt4)
        Assertions.assertEquals(bt, bt5)
        Assertions.assertEquals(bt, bt6)
        Assertions.assertEquals(bt, bt7)
        Assertions.assertEquals(bt, bt8)
        Assertions.assertEquals(bt, bt9)
        Assertions.assertEquals(bt, bt10)
        Assertions.assertEquals(bt, bt11)
    }

    fun between(solver: Solver, array: Boolean = false, `var`: Boolean = false): Double {
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
        return numVar1.value
    }

    @Test
    fun geIf() {
        val ge = geIf(solver = cbcSolver)
        val ge1 = geIf(solver = scipSolver)
        val ge2 = geIf(solver = coptSolver)
        Assertions.assertEquals(ge, ge1)
        Assertions.assertEquals(ge, ge2)
    }

    fun geIf(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun geIfNot() {
        val ge = geIfNot(solver = cbcSolver)
        val ge1 = geIfNot(solver = scipSolver)
        val ge2 = geIfNot(solver = coptSolver)
        Assertions.assertEquals(ge, ge1)
        Assertions.assertEquals(ge, ge2)
    }

    fun geIfNot(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun gtIf() {
        val gt = gtIf(solver = cbcSolver)
        val gt1 = gtIf(solver = scipSolver)
        val gt2 = gtIf(solver = coptSolver)
        Assertions.assertEquals(gt, gt1)
        Assertions.assertEquals(gt, gt2)
    }


    fun gtIf(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun gtIfNot() {
        val gt = gtIfNot(solver = cbcSolver)
        val gt1 = gtIfNot(solver = scipSolver)
        val gt2 = gtIfNot(solver = coptSolver)
        Assertions.assertEquals(gt, gt1)
        Assertions.assertEquals(gt, gt2)
    }

    fun gtIfNot(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun leIf() {
        val le = leIf(solver = coptSolver)
        val le1 = leIf(solver = scipSolver)
        val le2 = leIf(solver = cbcSolver)
        Assertions.assertEquals(le, le1)
        Assertions.assertEquals(le, le2)
    }


    fun leIf(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun leIfNot() {
        val le = leIfNot(solver = coptSolver)
        val le1 = leIfNot(solver = scipSolver)
        val le2 = leIfNot(solver = cbcSolver)
        Assertions.assertEquals(le, le1)
        Assertions.assertEquals(le, le2)
    }

    fun leIfNot(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun ltIf() {
        val lt = ltIf(solver = coptSolver)
        val lt1 = ltIf(solver = scipSolver)
        val lt2 = ltIf(solver = cbcSolver)
        Assertions.assertEquals(lt, lt1)
        Assertions.assertEquals(lt, lt2)
    }


    fun ltIf(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun ltIfNot() {
        val lt = ltIfNot(solver = coptSolver)
        val lt1 = ltIfNot(solver = scipSolver)
        val lt2 = ltIfNot(solver = cbcSolver)
        Assertions.assertEquals(lt, lt1)
        Assertions.assertEquals(lt, lt2)
    }

    fun ltIfNot(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun eqIf() {
        val eq = eqIf(solver = coptSolver)
        val eq1 = eqIf(solver = scipSolver)
        val eq2 = eqIf(solver = cbcSolver)
        Assertions.assertEquals(eq, eq1)
        Assertions.assertEquals(eq, eq2)
    }

    fun eqIf(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun eqIfNot() {
        val eq = eqIfNot(solver = coptSolver)
        val eq1 = eqIfNot(solver = scipSolver)
        val eq2 = eqIfNot(solver = cbcSolver)
        Assertions.assertEquals(eq, eq1)
        Assertions.assertEquals(eq, eq2)
    }

    fun eqIfNot(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun betweenIf() {
        val bt = betweenIf(solver = coptSolver)
        val bt1 = betweenIf(solver = scipSolver)
        val bt2 = betweenIf(solver = cbcSolver)
        Assertions.assertEquals(bt, bt1)
        Assertions.assertEquals(bt, bt2)
    }

    fun betweenIf(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun betweenIfNot() {
        val bt = betweenIfNot(solver = coptSolver)
        val bt1 = betweenIfNot(solver = scipSolver)
        val bt2 = betweenIfNot(solver = cbcSolver)
        Assertions.assertEquals(bt, bt1)
        Assertions.assertEquals(bt, bt2)
    }

    fun betweenIfNot(solver: Solver): Double {
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
        return numVar1.value
    }

    @Test
    fun atMostOne() {
        val m = atMostOne(solver = cbcSolver)
        val m1 = atMostOne(solver = scipSolver)
        val m2 = atMostOne(solver = coptSolver)
        Assertions.assertEquals(m, m1)
        Assertions.assertEquals(m, m2)
    }

    fun atMostOne(solver: Solver): Int {
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
        return size
    }

    @Test
    fun atMost() {
        val m = atMost(solver = cbcSolver)
        val m1 = atMost(solver = scipSolver)
        val m2 = atMost(solver = coptSolver)
        Assertions.assertEquals(m, m1)
        Assertions.assertEquals(m, m2)
    }

    fun atMost(solver: Solver): Int {
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
        return size
    }

    @Test
    fun atLeastOne() {
        val l = atLeastOne(solver = cbcSolver)
        val l1 = atLeastOne(solver = scipSolver)
        val l2 = atLeastOne(solver = coptSolver)
        Assertions.assertEquals(l, l1)
        Assertions.assertEquals(l, l2)
    }

    fun atLeastOne(solver: Solver): Int {
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
        return size
    }

    @Test
    fun atLeast() {
        val l = atLeast(solver = cbcSolver)
        val l1 = atLeast(solver = scipSolver)
        val l2 = atLeast(solver = coptSolver)
        Assertions.assertEquals(l, l1)
        Assertions.assertEquals(l, l2)
    }

    fun atLeast(solver: Solver): Int {
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
        return size
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