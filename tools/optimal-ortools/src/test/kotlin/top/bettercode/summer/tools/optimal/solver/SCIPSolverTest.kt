package top.bettercode.summer.tools.optimal.solver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.Solver
import top.bettercode.summer.tools.optimal.ortools.SCIPSolver

/**
 *
 * @author Peter Wu
 */
open class SCIPSolverTest {

    open val solver: Solver = SCIPSolver(epsilon = 1e-6)

    @Test
    fun operator() {
        solver.use {
            it.apply {
                val var1 = numVar(10.0, 10.0)
                val var2 = numVar(10.0, 10.0)
                val var3 = numVar(20.0, 20.0)
                val var4 = var1 + var2 * 2.0 + 10.0 + var3
                solve()
                System.err.println(var4.value)
                Assertions.assertEquals(60.0, var4.value)
            }
        }
    }

    @Test
    fun plus() {
        solver.use {
            it.apply {
                val var1 = numVar(10.0, 10.0)
                val var3 = numVar(20.0, 20.0)
                val var2 = var1.plus(-50.0)
                val var4 = var1.plus(var3)
                arrayOf(var1, var2).minimize()
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(var1.value)
                System.err.println(var2.value)
                System.err.println(var3.value)
                System.err.println(var4.value)
                Assertions.assertEquals(10.0, var1.value)
                Assertions.assertEquals(-40.0, var2.value)
                Assertions.assertEquals(20.0, var3.value)
                Assertions.assertEquals(30.0, var4.value)
            }
        }
    }

    @Test
    fun ge() {
        solver.use {
            it.ge()
            it.ge(`var` = true)
            it.ge(array = true)
            it.ge(array = true, `var` = true)
        }
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
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
        System.err.println(numVar1.value)
        Assertions.assertEquals(10.0, numVar1.value)
        return numVar1.value
    }

    @Test
    fun gt() {
        solver.use {
            it.gt()
            it.gt(`var` = true)
            it.gt(array = true)
            it.gt(array = true, `var` = true)
        }
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
        solver.use {
            it.le()
            it.le(`var` = true)
            it.le(array = true)
            it.le(array = true, `var` = true)
        }
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
        solver.use {
            it.lt()
            it.lt(`var` = true)
            it.lt(array = true)
            it.lt(array = true, `var` = true)
        }
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
    open fun ne() {
        solver.use {
            it.apply {
                val numVar1 = numVar(0.0, 100.0)
                arrayOf(numVar1).maximize()
                solve()
                System.err.println(numVar1.value)
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                Assertions.assertEquals(100.0, numVar1.value)
                numVar1.ne(100.0)
                arrayOf(numVar1).maximize()
                solve()
                System.err.println(numVar1.value)
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                Assertions.assertTrue(numVar1.value < 100.0)
            }
        }
    }

    @Test
    fun eq() {
        solver.use {
            it.eq()
            it.eq(`var` = true)
            it.eq(array = true)
            it.eq(array = true, `var` = true)
        }
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
        solver.use {
            it.between()
            it.between(`var` = true)
            it.between(array = true)
            it.between(array = true, `var` = true)
        }
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
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun geIf() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun geIfNot() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun gtIf() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun gtIfNot() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun leIf() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun leIfNot() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun ltIf() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun ltIfNot() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun eqIf() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun eqIfNot() {
        solver.use {
            it.apply {
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
            }
        }
    }

    @Test
    fun neIf() {
        solver.use {
            it.apply {
                val numVar1 = numVar(0.0, 100.0)
                val boolVar = boolVar()
                numVar1.neIf(100.0, boolVar)
                arrayOf(numVar1).maximize()
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(boolVar.value)
                System.err.println(numVar1.value)
                Assertions.assertTrue(numVar1.value == 100.0)
                arrayOf(numVar1).maximize()
                boolVar.eq(1.0)
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(boolVar.value)
                System.err.println(numVar1.value)
                Assertions.assertTrue(numVar1.value < 100.0)
            }
        }
    }

    @Test
    fun neIfNot() {
        solver.use {
            it.apply {
                val numVar1 = numVar(0.0, 100.0)
                val boolVar = boolVar()
                numVar1.neIfNot(100.0, boolVar)
                arrayOf(numVar1).maximize()
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(boolVar.value)
                System.err.println(numVar1.value)
                Assertions.assertTrue(numVar1.value == 100.0)
                arrayOf(numVar1).maximize()
                boolVar.eq(0.0)
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(boolVar.value)
                System.err.println(numVar1.value)
                Assertions.assertTrue(numVar1.value < 100.0)
            }
        }
    }

    @Test
    open fun condition() {
        solver.use {
            it.apply {
                System.err.println("===============${name}==============")
//        OptimalUtil.DEFAULT_EPSILON = 1e-6
                val numVar1 = numVar(0.0, 100.0)
                val numVar2 = numVar(0.0, 100.0)
                val onlyEnforceIf = numVar2.eqConst(20.0).onlyEnforceIf(numVar1.eqConst(10.0))
                numVar1.eq(10.0)
                arrayOf(numVar1, numVar2).maximize()
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(numVar1.value)
                System.err.println(numVar2.value)
                System.err.println(onlyEnforceIf?.value)
                Assertions.assertEquals(numVar1.value, 10.0)
                Assertions.assertEquals(numVar2.value, 20.0)
            }
        }
    }

    @Test
    open fun condition1() {
        solver.use {
            it.apply {
                System.err.println("===============${name}==============")
//        OptimalUtil.DEFAULT_EPSILON = 1e-6
                val numVar1 = numVar(0.0, 100.0)
                val numVar2 = numVar(0.0, 20.0)
                numVar1.eq(10.0)
                numVar2.neConst(20.0).onlyEnforceIf(numVar1.eqConst(10.0))
                arrayOf(numVar1, numVar2).maximize()
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(numVar1.value)
                System.err.println(numVar2.value)
                Assertions.assertEquals(numVar1.value, 10.0)
                Assertions.assertTrue(numVar2.value < 20.0)
            }
        }
    }


    @Test
    open fun condition2() {
        solver.use {
            it.apply {
                System.err.println("===============${name}==============")
//        OptimalUtil.DEFAULT_EPSILON = 1e-9
                val numVar1 = numVar(0.0, 100.0)
                val numVar2 = numVar(0.0, 20.0)
                numVar1.eq(10.0)
                numVar2.ltConst(20.0).onlyEnforceIf(numVar1.eqConst(10.0))
                arrayOf(numVar1, numVar2).maximize()
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(numVar1.value)
                System.err.println(numVar2.value)
                Assertions.assertTrue(numVar1.value == 10.0)
                Assertions.assertTrue(numVar2.value < 20.0)
            }
        }
    }

    @Test
    fun betweenIf() {
        solver.use { solv ->
            solv.apply {
                val numVar1 = numVar(0.0, 100.0)
                val boolVar = boolVar()
                numVar1.betweenIf(10.0, 20.0, boolVar)
                arrayOf(numVar1).maximize()
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(boolVar.value)
                System.err.println(numVar1.value)
                arrayOf(numVar1).minimize()
                boolVar.eq(1.0)
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                System.err.println(boolVar.value)
                System.err.println(numVar1.value)
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
            }
        }
    }

    @Test
    fun betweenIfNot() {
        solver.use { solv ->
            solv.apply {
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
            }
        }
    }

    @Test
    fun atMostOne() {
        solver.use { sol ->
            sol.apply {
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
            }
        }
    }

    @Test
    fun atMost() {
        solver.use { sol ->
            sol.apply {
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
            }
        }
    }

    @Test
    fun atLeastOne() {
        solver.use { sol ->
            sol.apply {
                val numVarArray = numVarArray(20, 0.0, 1000.0)
                numVarArray.atLeastOne()
                numVarArray.minimize()
                solve()
                Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
                numVarArray.forEach {
                    System.err.println(it.value)
                }
                val size = numVarArray.map { it.value }.filter { it > 0 }.size
                System.err.println(size)
                Assertions.assertTrue(size >= 1)
                Assertions.assertTrue(size <= 20)
            }
        }
    }

    @Test
    fun atLeast() {
        solver.use { sol ->
            sol.apply {
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
            }
        }
    }

    @Test
    fun lpNumVariables() {
        solver.use {
            it.lpNumVariables(10001)
        }
    }

    @Test
    fun numVariables() {
        solver.use {
            it.numVariables(2001)
        }
    }

    protected fun Solver.numVariables(num: Int) {
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
        val numVariables = numVariables()
        System.err.println("变量数量：" + numVariables + " 约束数量：" + numConstraints())
        solve()
        Assertions.assertTrue(numVariables == num)
        Assertions.assertTrue(numConstraints() == num)
        System.err.println(minimize.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
    }

    protected fun Solver.lpNumVariables(num: Int) {
        clear()
        val numVarArray = numVarArray(num, 0.0, 1000.0)
        for (i in 0 until num) {
            numVarArray[i].le(i.toDouble())
        }
        val minimize = numVarArray.minimize()
        System.err.println("变量数量：" + numVariables() + " 约束数量：" + numConstraints())
        solve()
        Assertions.assertTrue(numVariables() == num)
        Assertions.assertTrue(numConstraints() == num)
        System.err.println(minimize.value)
        Assertions.assertTrue(isOptimal(), "result:" + getResultStatus())
    }

}