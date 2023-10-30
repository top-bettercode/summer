package top.bettercode.summer.tools.optimal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.entity.ReqData
import top.bettercode.summer.tools.optimal.solver.CBCSolver
import top.bettercode.summer.tools.optimal.solver.COPTSolver
import top.bettercode.summer.tools.optimal.solver.SCIPSolver


/**
 * @author Peter Wu
 */
internal class RecipeSolverTest {

    @Test
    fun solve() {
        solve("13-05-07高氯枸磷")
        solve("24-06-10高氯枸磷")
        solve("15-15-15喷浆氯基")
        solve("15-15-15喷浆硫基")
        solve("15-15-15常规氯基")
    }

    fun solve(productName: String) {
        val reqData = ReqData(productName)
        val coptSolver = COPTSolver()
        val cbcSolver = CBCSolver()
        val scipSolver = SCIPSolver()
        val solve = RecipeSolver(coptSolver).solve(reqData)
        val solve1 = RecipeSolver(cbcSolver).solve(reqData)
        val solve2 = RecipeSolver(scipSolver).solve(reqData)
//        solve.toExcel()
//        solve1.toExcel()
//        solve2.toExcel()
        System.err.println("copt:"+solve.time)
        System.err.println("cbc:"+solve1.time)
        System.err.println("scip:"+solve2.time)
        Assertions.assertEquals(solve.recipes[0], solve1.recipes[0])
        Assertions.assertEquals(solve.recipeCount, solve1.recipeCount)
        Assertions.assertEquals(solve.recipes[0], solve2.recipes[0])
        Assertions.assertEquals(solve.recipeCount, solve2.recipeCount)
    }
}