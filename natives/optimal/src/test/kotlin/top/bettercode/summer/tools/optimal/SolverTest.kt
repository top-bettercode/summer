package top.bettercode.summer.tools.optimal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.solver.Solver
import top.bettercode.summer.tools.optimal.solver.SolverFactory
import top.bettercode.summer.tools.optimal.solver.SolverType

/**
 *
 * @author Peter Wu
 */
class SolverTest {

    private val cbcSolver: Solver = SolverFactory.createSolver(solverType = SolverType.CBC)
    private val scipSolver: Solver = SolverFactory.createSolver(solverType = SolverType.SCIP)
    private val coptSolver: Solver = SolverFactory.createSolver(solverType = SolverType.COPT, logging = true)

    @Test
    fun ge() {
        val ge = cbcSolver.ge()
        val ge1 = scipSolver.ge()
        val ge2 = coptSolver.ge()
        val ge3 = cbcSolver.ge(`var` = true)
        val ge4 = scipSolver.ge(`var` = true)
        val ge5 = coptSolver.ge(`var` = true)
        val ge6 = cbcSolver.ge(array = true)
        val ge7 = scipSolver.ge(array = true)
        val ge8 = coptSolver.ge(array = true)
        val ge9 = cbcSolver.ge(array = true, `var` = true)
        val ge10 = scipSolver.ge(array = true, `var` = true)
        val ge11 = coptSolver.ge(array = true, `var` = true)
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

    private fun Solver.ge(array: Boolean = false, `var`: Boolean = false): Double {
        val numVar1 = numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                arrayOf(numVar1).ge(numVar(10.0, 10.0))
            else
                arrayOf(numVar1).ge(10.0)
        } else {
            if (`var`)
                numVar1.ge(numVar(10.0, 10.0))
            else
                numVar1.ge(10.0)
        }
        arrayOf(numVar1).minimize()
        solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertEquals(10.0, numVar1.value)
        return numVar1.value
    }

    @Test
    fun gt() {
        val gt = cbcSolver.gt()
        val gt1 = scipSolver.gt()
        val gt2 = coptSolver.gt()
        val gt3 = cbcSolver.gt(`var` = true)
        val gt4 = scipSolver.gt(`var` = true)
        val gt6 = coptSolver.gt(`var` = true)
        val gt5 = cbcSolver.gt(array = true)
        val gt7 = scipSolver.gt(array = true)
        val gt8 = coptSolver.gt(array = true)
        val gt9 = cbcSolver.gt(array = true, `var` = true)
        val gt10 = scipSolver.gt(array = true, `var` = true)
        val gt11 = coptSolver.gt(array = true, `var` = true)
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

    private fun Solver.gt(array: Boolean = false, `var`: Boolean = false): Double {
        val numVar1 = numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                arrayOf(numVar1).gt(numVar(10.0, 10.0))
            else
                arrayOf(numVar1).gt(10.0)
        } else {
            if (`var`)
                numVar1.gt(numVar(10.0, 10.0))
            else
                numVar1.gt(10.0)
        }
        arrayOf(numVar1).minimize()
        solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value > 10.0)
        return numVar1.value
    }

    @Test
    fun le() {
        val le = cbcSolver.le()
        val le1 = scipSolver.le()
        val le2 = coptSolver.le()
        val le3 = cbcSolver.le(`var` = true)
        val le4 = scipSolver.le(`var` = true)
        val le5 = coptSolver.le(`var` = true)
        val le6 = cbcSolver.le(array = true)
        val le7 = scipSolver.le(array = true)
        val le8 = coptSolver.le(array = true)
        val le9 = cbcSolver.le(array = true, `var` = true)
        val le10 = scipSolver.le(array = true, `var` = true)
        val le11 = coptSolver.le(array = true, `var` = true)
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


    private fun Solver.le(array: Boolean = false, `var`: Boolean = false): Double {
        val numVar1 = numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                arrayOf(numVar1).le(numVar(10.0, 10.0))
            else
                arrayOf(numVar1).le(10.0)
        } else {
            if (`var`)
                numVar1.le(numVar(10.0, 10.0))
            else
                numVar1.le(10.0)
        }
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertEquals(10.0, numVar1.value)
        return numVar1.value
    }

    @Test
    fun lt() {
        val lt = cbcSolver.lt()
        val lt1 = scipSolver.lt()
        val lt2 = coptSolver.lt()
        val lt3 = cbcSolver.lt(`var` = true)
        val lt4 = scipSolver.lt(`var` = true)
        val lt5 = coptSolver.lt(`var` = true)
        val lt6 = cbcSolver.lt(array = true)
        val lt7 = scipSolver.lt(array = true)
        val lt8 = coptSolver.lt(array = true)
        val lt9 = cbcSolver.lt(array = true, `var` = true)
        val lt10 = scipSolver.lt(array = true, `var` = true)
        val lt11 = coptSolver.lt(array = true, `var` = true)
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

    private fun Solver.lt(array: Boolean = false, `var`: Boolean = false): Double {
        val numVar1 = numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                arrayOf(numVar1).lt(numVar(10.0, 10.0))
            else
                arrayOf(numVar1).lt(10.0)
        } else {
            if (`var`)
                numVar1.lt(numVar(10.0, 10.0))
            else
                numVar1.lt(10.0)
        }
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value < 10.0)
        return numVar1.value
    }

    @Test
    fun eq() {
        val eq = cbcSolver.eq()
        val eq1 = scipSolver.eq()
        val eq2 = coptSolver.eq()
        val eq3 = cbcSolver.eq(`var` = true)
        val eq4 = scipSolver.eq(`var` = true)
        val eq5 = coptSolver.eq(`var` = true)
        val eq6 = cbcSolver.eq(array = true)
        val eq7 = scipSolver.eq(array = true)
        val eq8 = coptSolver.eq(array = true)
        val eq9 = cbcSolver.eq(array = true, `var` = true)
        val eq10 = scipSolver.eq(array = true, `var` = true)
        val eq11 = coptSolver.eq(array = true, `var` = true)
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


    private fun Solver.eq(array: Boolean = false, `var`: Boolean = false): Double {
        val numVar1 = numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                arrayOf(numVar1).eq(numVar(10.0, 10.0))
            else
                arrayOf(numVar1).eq(10.0)
        } else {
            if (`var`)
                numVar1.eq(numVar(10.0, 10.0))
            else
                numVar1.eq(10.0)
        }
        arrayOf(numVar1).minimize()
        solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertEquals(10.0, numVar1.value)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertEquals(10.0, numVar1.value)
        return numVar1.value
    }

    @Test
    fun between() {
        val bt = cbcSolver.between()
        val bt1 = scipSolver.between()
        val bt2 = coptSolver.between()
        val bt3 = cbcSolver.between(`var` = true)
        val bt4 = scipSolver.between(`var` = true)
        val bt5 = coptSolver.between(`var` = true)
        val bt6 = cbcSolver.between(array = true)
        val bt7 = scipSolver.between(array = true)
        val bt8 = coptSolver.between(array = true)
        val bt9 = cbcSolver.between(array = true, `var` = true)
        val bt10 = scipSolver.between(array = true, `var` = true)
        val bt11 = coptSolver.between(array = true, `var` = true)
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

    private fun Solver.between(array: Boolean = false, `var`: Boolean = false): Double {
        val numVar1 = numVar(0.0, 100.0)
        if (array) {
            if (`var`)
                arrayOf(numVar1).between(numVar(10.0, 10.0), numVar(20.0, 20.0))
            else
                arrayOf(numVar1).between(10.0, 20.0)
        } else {
            if (`var`)
                numVar1.between(numVar(10.0, 10.0), numVar(20.0, 20.0))
            else
                numVar1.between(10.0, 20.0)
        }
        arrayOf(numVar1).minimize()
        solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value >= 10.0)
        Assertions.assertTrue(numVar1.value <= 20.0)
        Assertions.assertTrue(numVar1.value == 10.0)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value >= 10.0)
        Assertions.assertTrue(numVar1.value <= 20.0)
        Assertions.assertTrue(numVar1.value == 20.0)
        return numVar1.value
    }

    @Test
    fun ratioInRange() {
        val ratioInRange = cbcSolver.ratioInRange()
        val ratioInRange1 = scipSolver.ratioInRange()
        val ratioInRange2 = coptSolver.ratioInRange()
        Assertions.assertEquals(ratioInRange, ratioInRange1)
        Assertions.assertEquals(ratioInRange, ratioInRange2)
    }

    private fun Solver.ratioInRange(): Double {
        val part = numVar(0.0, 100.0)
        val whole = numVar(10.0, 100.0)
        part.ratioInRange(whole, 0.4, 0.6)
        arrayOf(part, whole).minimize()
        solve()
        System.err.println(part.value)
        System.err.println(whole.value)
        System.err.println(part.value / whole.value)
        Assertions.assertTrue(part.value / whole.value in 0.4..0.6)
        arrayOf(part, whole).maximize()
        solve()
        System.err.println(part.value)
        System.err.println(whole.value)
        System.err.println(part.value / whole.value)
        Assertions.assertTrue(part.value / whole.value in 0.4..0.6)
        return part.value / whole.value
    }

    @Test
    fun geIf() {
        val ge = cbcSolver.geIf()
        val ge1 = scipSolver.geIf()
        val ge2 = coptSolver.geIf()
        Assertions.assertEquals(ge, ge1)
        Assertions.assertEquals(ge, ge2)
    }

    private fun Solver.geIf(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val numVar2 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.geIf(10.0, boolVar)
        numVar1.ltIfNot(10.0, boolVar)
        numVar2.geIf(10.0, boolVar)
        arrayOf(numVar1, numVar2).minimize()
        solve()
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        System.err.println(numVar2.value)
        numVar1.eq(10.1)
        arrayOf(numVar1, numVar2).minimize()
        solve()
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        System.err.println(numVar2.value)
        Assertions.assertTrue(numVar2.value >= 10.0)
        return numVar2.value
    }

    @Test
    fun geIfNot() {
        val ge = cbcSolver.geIfNot()
        val ge1 = scipSolver.geIfNot()
        val ge2 = coptSolver.geIfNot()
        Assertions.assertEquals(ge, ge1)
        Assertions.assertEquals(ge, ge2)
    }

    private fun Solver.geIfNot(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.geIfNot(10.0, boolVar)
        arrayOf(numVar1).minimize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).minimize()
        boolVar.eq(0.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value >= 10.0)
        return numVar1.value
    }

    @Test
    fun gtIf() {
        val gt = cbcSolver.gtIf()
        val gt1 = scipSolver.gtIf()
        val gt2 = coptSolver.gtIf()
        Assertions.assertEquals(gt, gt1)
        Assertions.assertEquals(gt, gt2)
    }


    private fun Solver.gtIf(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.gtIf(10.0, boolVar)
        arrayOf(numVar1).minimize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).minimize()
        boolVar.eq(1.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value > 10.0)
        return numVar1.value
    }

    @Test
    fun gtIfNot() {
        val gt = cbcSolver.gtIfNot()
        val gt1 = scipSolver.gtIfNot()
        val gt2 = coptSolver.gtIfNot()
        Assertions.assertEquals(gt, gt1)
        Assertions.assertEquals(gt, gt2)
    }

    private fun Solver.gtIfNot(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.gtIfNot(10.0, boolVar)
        arrayOf(numVar1).minimize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).minimize()
        boolVar.eq(0.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value > 10.0)
        return numVar1.value
    }

    @Test
    fun leIf() {
        val le = coptSolver.leIf()
        val le1 = scipSolver.leIf()
        val le2 = cbcSolver.leIf()
        Assertions.assertEquals(le, le1)
        Assertions.assertEquals(le, le2)
    }


    private fun Solver.leIf(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.leIf(10.0, boolVar)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).maximize()
        boolVar.eq(1.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value <= 10.0)
        return numVar1.value
    }

    @Test
    fun leIfNot() {
        val le = coptSolver.leIfNot()
        val le1 = scipSolver.leIfNot()
        val le2 = cbcSolver.leIfNot()
        Assertions.assertEquals(le, le1)
        Assertions.assertEquals(le, le2)
    }

    private fun Solver.leIfNot(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.leIfNot(10.0, boolVar)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).maximize()
        boolVar.eq(0.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value <= 10.0)
        return numVar1.value
    }

    @Test
    fun ltIf() {
        val lt = coptSolver.ltIf()
        val lt1 = scipSolver.ltIf()
        val lt2 = cbcSolver.ltIf()
        Assertions.assertEquals(lt, lt1)
        Assertions.assertEquals(lt, lt2)
    }


    private fun Solver.ltIf(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.ltIf(10.0, boolVar)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).maximize()
        boolVar.eq(1.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value < 10.0)
        return numVar1.value
    }

    @Test
    fun ltIfNot() {
        val lt = coptSolver.ltIfNot()
        val lt1 = scipSolver.ltIfNot()
        val lt2 = cbcSolver.ltIfNot()
        Assertions.assertEquals(lt, lt1)
        Assertions.assertEquals(lt, lt2)
    }

    private fun Solver.ltIfNot(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.ltIfNot(10.0, boolVar)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).maximize()
        boolVar.eq(0.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value < 10.0)
        return numVar1.value
    }

    @Test
    fun eqIf() {
        val eq = coptSolver.eqIf()
        val eq1 = scipSolver.eqIf()
        val eq2 = cbcSolver.eqIf()
        Assertions.assertEquals(eq, eq1)
        Assertions.assertEquals(eq, eq2)
    }

    private fun Solver.eqIf(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.eqIf(10.0, boolVar)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).maximize()
        boolVar.eq(1.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value == 10.0)
        return numVar1.value
    }

    @Test
    fun eqIfNot() {
        val eq = coptSolver.eqIfNot()
        val eq1 = scipSolver.eqIfNot()
        val eq2 = cbcSolver.eqIfNot()
        Assertions.assertEquals(eq, eq1)
        Assertions.assertEquals(eq, eq2)
    }

    private fun Solver.eqIfNot(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.eqIfNot(10.0, boolVar)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).maximize()
        boolVar.eq(0.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value == 10.0)
        return numVar1.value
    }

    @Test
    fun betweenIf() {
        val bt = coptSolver.betweenIf()
        val bt1 = scipSolver.betweenIf()
        val bt2 = cbcSolver.betweenIf()
        Assertions.assertEquals(bt, bt1)
        Assertions.assertEquals(bt, bt2)
    }

    private fun Solver.betweenIf(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.betweenIf(10.0, 20.0, boolVar)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).minimize()
        boolVar.eq(1.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value in 10.0..20.0)
        Assertions.assertTrue(numVar1.value == 10.0)
        arrayOf(numVar1).maximize()
        boolVar.eq(1.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value in 10.0..20.0, name)
        Assertions.assertTrue(numVar1.value == 20.0)
        return numVar1.value
    }

    @Test
    fun betweenIfNot() {
        val bt = coptSolver.betweenIfNot()
        val bt1 = scipSolver.betweenIfNot()
        val bt2 = cbcSolver.betweenIfNot()
        Assertions.assertEquals(bt, bt1)
        Assertions.assertEquals(bt, bt2)
    }

    private fun Solver.betweenIfNot(): Double {
        val numVar1 = numVar(0.0, 100.0)
        val boolVar = boolVar()
        numVar1.betweenIfNot(10.0, 20.0, boolVar)
        arrayOf(numVar1).maximize()
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        arrayOf(numVar1).minimize()
        boolVar.eq(0.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value in 10.0..20.0)
        Assertions.assertTrue(numVar1.value == 10.0)
        arrayOf(numVar1).maximize()
        boolVar.eq(0.0)
        solve()
        System.err.println(boolVar.value)
        System.err.println(numVar1.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(numVar1.value in 10.0..20.0)
        Assertions.assertTrue(numVar1.value == 20.0)
        return numVar1.value
    }

    @Test
    fun atMostOne() {
        val m = cbcSolver.atMostOne()
        val m1 = scipSolver.atMostOne()
        val m2 = coptSolver.atMostOne()
        Assertions.assertEquals(m, m1)
        Assertions.assertEquals(m, m2)
    }

    private fun Solver.atMostOne(): Int {
        val numVarArray = numVarArray(20, 0.0, 1000.0)
        numVarArray.atMostOne()
        numVarArray.maximize()
        solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertEquals(size, 1)
        return size
    }

    @Test
    fun atMost() {
        val m = cbcSolver.atMost()
        val m1 = scipSolver.atMost()
        val m2 = coptSolver.atMost()
        Assertions.assertEquals(m, m1)
        Assertions.assertEquals(m, m2)
    }

    private fun Solver.atMost(): Int {
        val numVarArray = numVarArray(20, 0.0, 1000.0)
        numVarArray.atMost(5)
        numVarArray.maximize()
        solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertEquals(size, 5)
        return size
    }

    @Test
    fun atLeastOne() {
        val l = cbcSolver.atLeastOne()
        val l1 = scipSolver.atLeastOne()
        val l2 = coptSolver.atLeastOne()
        Assertions.assertEquals(l, l1)
        Assertions.assertEquals(l, l2)
    }

    private fun Solver.atLeastOne(): Int {
        val numVarArray = numVarArray(20, 0.0, 1000.0)
        numVarArray.atLeastOne()
        numVarArray.minimize()
        solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(size >= 1)
        Assertions.assertTrue(size <= 20)
        return size
    }

    @Test
    fun atLeast() {
        val l = cbcSolver.atLeast()
        val l1 = scipSolver.atLeast()
        val l2 = coptSolver.atLeast()
        Assertions.assertEquals(l, l1)
        Assertions.assertEquals(l, l2)
    }

    private fun Solver.atLeast(): Int {
        val numVarArray = numVarArray(20, 0.0, 1000.0)
        numVarArray.atLeast(5)
        numVarArray.minimize()
        solve()
        numVarArray.forEach {
            System.err.println(it.value)
        }
        val size = numVarArray.map { it.value }.filter { it > 0 }.size
        System.err.println(size)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        Assertions.assertTrue(size >= 5)
        Assertions.assertTrue(size <= 20)
        return size
    }

    @Test
    fun lpNumVariables() {
        coptSolver.lpNumVariables(10000)
        Assertions.assertThrows(copt.CoptException::class.java) {
            coptSolver.lpNumVariables(10001)
        }
        cbcSolver.lpNumVariables(10001)
        scipSolver.lpNumVariables(10001)
    }

    @Test
    fun numVariables() {
        coptSolver.numVariables(2000)
        Assertions.assertThrows(copt.CoptException::class.java) {
            coptSolver.numVariables(2001)
        }
        cbcSolver.numVariables(2001)
        scipSolver.numVariables(2001)
    }

    private fun Solver.numVariables(num: Int) {
        clear()
        val numVarArray = numVarArray(num / 2, 0.0, 1000.0)
        val intVarArray = intVarArray(num - num / 2, 0.0, 1000.0)
        for (i in 0 until num / 2) {
            numVarArray[i].le(i.toDouble())
        }
        for (i in 0 until num - num / 2) {
            intVarArray[i].le(i.toDouble())
        }
        val minimize = numVarArray.minimize()
        System.err.println("变量数量：" + numVariables() + " 约束数量：" + numConstraints())
        Assertions.assertTrue(numVariables() == num)
        Assertions.assertTrue(numConstraints() == num)
        solve()
        System.err.println(minimize.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
    }

    private fun Solver.lpNumVariables(num: Int) {
        clear()
        val numVarArray = numVarArray(num, 0.0, 1000.0)
        for (i in 0 until num) {
            numVarArray[i].le(i.toDouble())
        }
        val minimize = numVarArray.minimize()
        System.err.println("变量数量：" + numVariables() + " 约束数量：" + numConstraints())
        Assertions.assertTrue(numVariables() == num)
        Assertions.assertTrue(numConstraints() == num)
        solve()
        System.err.println(minimize.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
    }

}