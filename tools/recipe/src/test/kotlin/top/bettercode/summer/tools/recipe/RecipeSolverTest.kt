package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.solver.SolverFactory
import top.bettercode.summer.tools.optimal.solver.SolverType
import top.bettercode.summer.tools.recipe.data.PrepareData
import top.bettercode.summer.tools.recipe.data.RecipeMaterialView
import top.bettercode.summer.tools.recipe.data.RecipeResult
import top.bettercode.summer.tools.recipe.data.RecipeView
import top.bettercode.summer.tools.recipe.material.IRecipeMaterial
import top.bettercode.summer.tools.recipe.result.Recipe
import java.io.File


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
        val requirement = PrepareData.readRequirement(productName)
        val maxResult = 20
        val coptSolver = SolverFactory.createSolver(SolverType.COPT)
        val cbcSolver = SolverFactory.createSolver(SolverType.CBC)
        val scipSolver = SolverFactory.createSolver(SolverType.SCIP)
        val solve = MultiRecipeSolver.solve(solver = coptSolver, requirement = requirement, maxResult = maxResult)
        val solve1 = MultiRecipeSolver.solve(solver = cbcSolver, requirement = requirement, maxResult = maxResult)
        val solve2 = MultiRecipeSolver.solve(solver = scipSolver, requirement = requirement, maxResult = maxResult)
        solve.toExcel()
//        solve1.toExcel()
//        solve2.toExcel()
        System.err.println("copt:" + solve.time)
        System.err.println("cbc:" + solve1.time)
        System.err.println("scip:" + solve2.time)
        System.err.println(json(solve.recipes[0], true))
        validate(solve)
        validate(solve1)
        validate(solve2)

        assert(solve, solve1)
        assert(solve, solve2)
        assert(solve1, solve2)

//        saveRecipe(solve)
//        saveRecipe(solve1)
//        saveRecipe(solve2)

    }

    private fun validate(recipeResult: RecipeResult) {
        for (recipe in recipeResult.recipes) {
            Assertions.assertTrue(recipe.validate())
        }
        val recipes = recipeResult.recipes
        val requirement = recipes[0].requirement
        val productName = requirement.productName

        val expectedRequirement = RecipeResult::class.java.getResourceAsStream("/recipe/$productName/requirement.json")!!.bufferedReader().readText()
        //配方要求
        Assertions.assertEquals(expectedRequirement, json(requirement))
        //配方
        val dir = "recipe/$productName/${recipeResult.solverName}"
        recipes.forEachIndexed { index, recipe ->
            val expectedRecipe = RecipeResult::class.java.getResourceAsStream("/$dir/配方${index + 1}.json")!!.bufferedReader().readText()
            Assertions.assertEquals(expectedRecipe, json(recipe, true))
        }
    }

    private fun assert(solve: RecipeResult, solve1: RecipeResult) {
        Assertions.assertEquals(solve.recipes[0].cost, solve1.recipes[0].cost)
        Assertions.assertEquals(solve.recipes.size, solve1.recipes.size)
        Assertions.assertEquals(json(solve.recipes[0].materials), json(solve1.recipes[0].materials))
    }

    fun json(value: Any, useView: Boolean = false): String {
        val objectMapper = ObjectMapper()
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        if (useView) {
            val simpleModule = SimpleModule()
            simpleModule.setMixInAnnotation(IRecipeMaterial::class.java, RecipeMaterialView::class.java)
            simpleModule.setMixInAnnotation(Recipe::class.java, RecipeView::class.java)
            objectMapper.registerModule(simpleModule)
        }
        return objectMapper.writeValueAsString(value)
    }

    fun saveRecipe(recipeResult: RecipeResult) {
        val recipes = recipeResult.recipes
        val requirement = recipes[0].requirement
        val productName = requirement.productName
        val dir = File("recipe/$productName/${recipeResult.solverName}")
        dir.mkdirs()
        //配方要求
        File("recipe/$productName/requirement.json").writeText(json(requirement))
        //配方
        recipes.forEachIndexed { index, recipe ->
            File("$dir/配方${index + 1}.json").writeText(json(recipe, true))
        }
    }
}