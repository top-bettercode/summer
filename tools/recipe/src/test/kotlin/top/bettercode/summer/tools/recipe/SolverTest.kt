package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.databind.type.TypeFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.tools.lang.util.FileUtil
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.optimal.SolverType
import top.bettercode.summer.tools.optimal.copt.COPTSolver
import top.bettercode.summer.tools.optimal.cplex.CplexSolver
import top.bettercode.summer.tools.optimal.gurobi.GurobiSolver
import top.bettercode.summer.tools.optimal.ortools.CBCSolver
import top.bettercode.summer.tools.optimal.ortools.SCIPSolver
import top.bettercode.summer.tools.recipe.data.OptimalLineRequire
import top.bettercode.summer.tools.recipe.result.Recipe
import top.bettercode.summer.tools.recipe.result.RecipeResult
import java.io.File

/**
 *
 * @author Peter Wu
 */
class SolverTest {
    val epsilon = 1e-2
    val minEpsilon = 1e-2

    val openExcel = false
    //    val openExcel = true

    val solverTypes = listOf(
        SolverType.COPT,
        SolverType.CPLEX,
        SolverType.SCIP,
        SolverType.CBC,
//        SolverType.GUROBI,
    )

    @Disabled
    @Test
    fun compareTo() {
//        val require = "cbc-1e-4-error" // eqIfNot 使用中间变量解决
//        val require = "cbc-1e-4-error2" // eqIfNot 不使用中间变量解决
//        val require = "cbc-1e-4-fail" //
//        val require = "cbc-1e-4-fail2" //
        val require = "test"

        val content =
            File("${FileUtil.userDir}/src/test/resources/require/$require.json").readText()

        val requirement = RecipeRequirement.read(
            content
        )
        solve(requirement)
    }

    //    @Disabled
    @Test
    fun all() {
        all("p_optimal_line_require.json") {
            //529
//            arrayOf(529L).contains(it)
            true
        }
        all("p_optimal_line_require_release.json") {
//            arrayOf(342L).contains(it)
            true
        }
    }

    private fun all(requiresJson: String, filter: (Long) -> Boolean) {
        val inputStream = ClassPathResource(requiresJson).inputStream
        val type = TypeFactory.defaultInstance()
            .constructCollectionType(List::class.java, OptimalLineRequire::class.java)
        val requires: List<OptimalLineRequire> = StringUtil.readJson(inputStream.readBytes(), type)
        requires.sortedBy { it.optimalLineId }
            .filter { filter(it.optimalLineId!!) }
            .map { RecipeRequirement.read(it.requirement!!) }.forEach {
                solve(it)
            }
    }


    private fun solve(
        requirement: RecipeRequirement,
    ) {
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
        for (it in solvers) {
            val solved = RecipeSolver.solve(
                solver = it,
                requirement = requirement,
                minMaterialNum = false,
                minEpsilon = minEpsilon
            )
            try {
                solved?.validate()
            } catch (e: Exception) {
                it.write("$dir/${requirement.id}.lp")
                failMsgs.add("${it.name} " + e.message!!)
            }
            if (lastSolved != null) {
                try {
                    lastSolved.compareTo(solved)
                } catch (e: Exception) {
                    it.write("$dir/${requirement.id}.lp")
                    failMsgs.add("${it.name} " + e.message!!)
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

    val dir = "${FileUtil.userDir}/build/lp"

    @BeforeEach
    fun setUp() {
        File(dir).mkdirs()
    }
}