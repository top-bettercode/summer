package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.databind.type.TypeFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
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
@Disabled
class SolverTest {
    val epsilon = 1e-2
    val openExcel = false
    val minEpsilon = 1e-3
    val dir = "${System.getProperty("user.dir")}/build/lp"
    val solverTypes = listOf(
        SolverType.COPT,
        SolverType.GUROBI,
        SolverType.CPLEX,
        SolverType.SCIP,
        SolverType.CBC,
    )

    @Test
    fun compareTo() {
        val require = "copt-1e-2-error" // eqIfNot 使用中间变量解决
//        val require = "cbc-1e-4-error" // eqIfNot 使用中间变量解决
//        val require = "cbc-1e-4-error2" // eqIfNot 不使用中间变量解决

        val content =
            File("${System.getProperty("user.dir")}/src/test/resources/require/$require.json").readText()

        val requirement = RecipeRequirement.read(
            content
        )
        solve(requirement)
    }

    @Test
    fun all() {
        val inputStream = ClassPathResource("p_optimal_line_require.json").inputStream
        val type = TypeFactory.defaultInstance()
            .constructCollectionType(List::class.java, OptimalLineRequire::class.java)
        val requires: List<OptimalLineRequire> = StringUtil.readJson(inputStream.readBytes(), type)
        requires.map { RecipeRequirement.read(it.requirement!!) }.forEach {
            solve(it)
        }
    }

    private fun solve(
        requirement: RecipeRequirement
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
                it.write("$dir/${requirement.id}.lp")
                failMsgs.add(e.message!!)
            }
            if (lastSolved != null) {
                try {
                    lastSolved!!.compareTo(solved)
                } catch (e: Exception) {
                    it.write("$dir/${requirement.id}.lp")
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

    @BeforeEach
    fun setUp() {
        File(dir).mkdirs()
    }
}