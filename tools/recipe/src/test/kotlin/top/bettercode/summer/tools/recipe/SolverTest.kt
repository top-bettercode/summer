package top.bettercode.summer.tools.recipe

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.SolverType
import top.bettercode.summer.tools.optimal.copt.COPTSolver
import top.bettercode.summer.tools.optimal.cplex.CplexSolver
import top.bettercode.summer.tools.optimal.gurobi.GurobiSolver
import top.bettercode.summer.tools.optimal.ortools.CBCSolver
import top.bettercode.summer.tools.optimal.ortools.SCIPSolver
import top.bettercode.summer.tools.recipe.result.Recipe
import java.io.File

/**
 *
 * @author Peter Wu
 */
class SolverTest {
    val epsilon = 1e-3
    val minEpsilon = 0.0
    val solverTypes = listOf(
        SolverType.COPT,
        SolverType.GUROBI,
        SolverType.SCIP,
        SolverType.CBC,
        SolverType.CPLEX,
    )

    @Test
    fun cplex() {
        //读取 require/cplex-notMix-test.json
        val content =
            File("${System.getProperty("user.dir")}/src/test/resources/require/cplex-notMix-test.json").readText()

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
        var lastSolved: Recipe? = null
        solvers.forEach {
            val solved = RecipeSolver.solve(
                solver = it,
                requirement = requirement,
                minMaterialNum = true,
                minEpsilon = minEpsilon
            )
            solved?.validate()
            if (lastSolved != null) {
                lastSolved!!.compareTo(solved)
            }
            if (solved != null) {
                lastSolved = solved
            }
        }
    }

    @Test
    fun scip() {
        val content =
            File("${System.getProperty("user.dir")}/src/test/resources/require/scip-fail-test.json").readText()

        val requirement = RecipeRequirement.read(
            content
        )
        val solver = SCIPSolver(epsilon = epsilon, minEpsilon = epsilon)
        val solved = RecipeSolver.solve(
            solver = solver,
            requirement = requirement,
            minMaterialNum = true,
            minEpsilon = minEpsilon
        )
        solver.writeLp("${System.getProperty("user.dir")}/src/test/resources/require/scip-fail-test.lp")
        solved?.validate()

    }
}