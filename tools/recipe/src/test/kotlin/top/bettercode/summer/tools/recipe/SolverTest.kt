package top.bettercode.summer.tools.recipe

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.SolverType
import top.bettercode.summer.tools.optimal.copt.COPTSolver
import top.bettercode.summer.tools.optimal.cplex.CplexSolver
import top.bettercode.summer.tools.optimal.gurobi.GurobiSolver
import top.bettercode.summer.tools.optimal.ortools.CBCSolver
import top.bettercode.summer.tools.optimal.ortools.SCIPSolver
import top.bettercode.summer.tools.recipe.result.Recipe
import top.bettercode.summer.tools.recipe.result.RecipeResult
import java.io.File

/**
 *
 * @author Peter Wu
 */
@Disabled
class SolverTest {
    val epsilon = 1e-3
    val openExcel = false
    val minEpsilon = 1e-3
    val solverTypes = listOf(
        SolverType.COPT,
        SolverType.GUROBI,
        SolverType.CPLEX,
        SolverType.SCIP,
        SolverType.CBC,
    )

    @Test
    fun compareTo() {
//        val epsilon = 1e-2
//        val require = "scip-1e-2-fail-98" //降级 ortools-java:9.9.3963 解决

//        val epsilon = 1e-3
//        val require = "cplex-1e-3-notMix"  //         model.setParam(Param.MIP.Tolerances.MIPGap, 1e-9) 解决
//        val require = "cplex-1e-3-error"  //         model.setParam(Param.MIP.Tolerances.MIPGap, 1e-9) 解决
//        val require = "gurobi-1e-3-error" // 取消FeasibilityTol设置解决
//        val require = "scip-1e-3-fail" //升级ortools-java:9.10.4067 解决
        val epsilon = 1e-4
//        val require = "copt-1e-4-fail" //兼容 minMaterialNum失败 解决
//        val require = "cbc-1e-4-fail" // 求解器eqIf方法修改解决
//        val require = "cbc-1e-4-error" // eqIfNot 使用中间变量解决
//        val require = "scip-1e-4-fail-99" //降级 ortools-java:9.8.3296 解决
//        val require = "cbc-1e-4-fail" //未解决
        val require = "copt-1e-4-fail" //兼容 minMaterialNum失败 解决

//        val require = "gurobi-1e-4-maxUseMaterialNum" //进料口限制失败， set(GRB.DoubleParam.OptimalityTol, 1e-9) 解决
        val content =
            File("${System.getProperty("user.dir")}/src/test/resources/require/$require.json").readText()

        val requirement = RecipeRequirement.read(
            content
        )
        val solvers = solverTypes.map {
            when (it) {
                SolverType.COPT -> COPTSolver(epsilon = epsilon, minEpsilon = epsilon)
                SolverType.GUROBI -> GurobiSolver(epsilon = epsilon, minEpsilon = epsilon)
                SolverType.SCIP -> SCIPSolver(epsilon = epsilon, minEpsilon = epsilon)
                SolverType.CBC -> CBCSolver(epsilon = epsilon, minEpsilon = epsilon)
                SolverType.CPLEX -> CplexSolver(epsilon = epsilon, minEpsilon = epsilon)
            }
        }
        val outFile = File("build/recipe/推优结果" + System.currentTimeMillis() + ".xlsx")
        outFile.parentFile.mkdirs()
        val recipeResult = RecipeResult(requirement)
        val failMsgs = mutableListOf<String>()
        var lastSolved: Recipe? = null
        solvers.forEach {
            val solved = RecipeSolver.solve(
                solver = it,
                requirement = requirement,
                minMaterialNum = true,
                minEpsilon = minEpsilon
            )
            try {
                solved?.validate()
            } catch (e: Exception) {
                it.write("${System.getProperty("user.dir")}/build/$require.lp")
                failMsgs.add(e.message!!)
            }
            if (lastSolved != null) {
                try {
                    lastSolved!!.compareTo(solved)
                } catch (e: Exception) {
                    it.write("${System.getProperty("user.dir")}/build/$require.lp")
                    failMsgs.add(e.message!!)
                }
            }
            if (solved != null) {
                recipeResult.addRecipe(solved)
                lastSolved = solved
            }
        }
        recipeResult.toExcel(outFile)
        if (openExcel)
            Runtime.getRuntime().exec(arrayOf("xdg-open", outFile.absolutePath))
        System.err.println("------------------------------------------------------")
        failMsgs.forEach {
            System.err.println(it)
            System.err.println("------------------------------------------------------")
        }
        Assertions.assertTrue(failMsgs.isEmpty())
    }

    @Test
    fun singe() {
//        val epsilon = 1e-3
//        val require = "scip-1e-3-fail" //升级ortools-java:9.10.4067 解决
//        val epsilon = 1e-2
//        val require = "scip-1e-2-fail-98" //降级 ortools-java:9.9.3963 解决
        val epsilon = 1e-4
//        val require = "scip-1e-4-fail-99" //降级 ortools-java:9.8.3296 解决
        val require = "cbc-1e-4-fail" //未解决
//        val require = "copt-1e-4-fail" //兼容 minMaterialNum失败 解决

        val content =
            File("${System.getProperty("user.dir")}/src/test/resources/require/$require.json").readText()

        val requirement = RecipeRequirement.read(
            content
        )
//        val solver = SCIPSolver(epsilon = epsilon, minEpsilon = epsilon)
        val solver = CBCSolver(epsilon = epsilon, minEpsilon = epsilon)
//        val solver = COPTSolver(epsilon = epsilon, minEpsilon = epsilon)
        val solved = RecipeSolver.solve(
            solver = solver,
            requirement = requirement,
            minMaterialNum = true,
//            includeProductionCost = false,
            minEpsilon = minEpsilon
        )
        solver.write("${System.getProperty("user.dir")}/build/$require.lp")
        solved?.validate()
        Assertions.assertNotNull(solved)
    }
}