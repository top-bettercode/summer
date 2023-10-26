package top.bettercode.summer.tools.optimal

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.entity.ReqData
import top.bettercode.summer.tools.optimal.solver.COPTSolver


/**
 * @author Peter Wu
 */
@Disabled
internal class RecipeSolverTest {

    @Test
    fun solve() {
        val solver = COPTSolver()
//        val solver = CBCSolver()
//        val solver = SCIPSolver()
        val reqData = ReqData("13-05-07高氯枸磷")
//        val reqData = ReqData("24-06-10高氯枸磷")
//        val reqData = ReqData("15-15-15喷浆氯基")
//        val reqData = ReqData("15-15-15喷浆硫基")
//        val reqData = ReqData("15-15-15常规氯基")

//        reqData.numMaxMaterials = 7
//        reqData.isLimitResultNutrient = false
        RecipeSolver(solver).solve(reqData).toExcel()
    }

}