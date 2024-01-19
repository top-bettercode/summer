package top.bettercode.summer.tools.optimal

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.entity.ReqData
import top.bettercode.summer.tools.optimal.solver.SolverFactory
import top.bettercode.summer.tools.optimal.solver.SolverType


/**
 * @author Peter Wu
 */
internal class RecipeSolverTest {

    @Test
    fun solve() {
        solve("13-05-07高氯枸磷")
//        solve("24-06-10高氯枸磷")
//        solve("15-15-15喷浆氯基")
//        solve("15-15-15喷浆硫基")
//        solve("15-15-15常规氯基")
    }

    fun solve(productName: String) {
        val reqData = ReqData(productName)
        val coptSolver = SolverFactory.createSolver(SolverType.COPT)
        val cbcSolver = SolverFactory.createSolver(SolverType.CBC)
        val scipSolver = SolverFactory.createSolver(SolverType.SCIP)
//        val cpSolver = SolverFactory.createSolver(SolverType.CP)
        val solve = RecipeSolver(coptSolver).solve(reqData)
        val solve1 = RecipeSolver(cbcSolver).solve(reqData)
        val solve2 = RecipeSolver(scipSolver).solve(reqData)
//        val solve3 = RecipeSolver(cpSolver).solve(reqData)
//        solve.toExcel()
//        solve1.toExcel()
//        solve2.toExcel()
//        solve3.toExcel()
        System.err.println("copt:" + solve.time)
        System.err.println("cbc:" + solve1.time)
        System.err.println("scip:" + solve2.time)
        System.err.println("cp:" + solve2.time)
        Assertions.assertEquals(solve.recipes[0], solve1.recipes[0])
        Assertions.assertEquals(solve.recipeCount, solve1.recipeCount)
        Assertions.assertEquals(solve.recipes[0], solve2.recipes[0])
        Assertions.assertEquals(solve.recipeCount, solve2.recipeCount)
    }
}