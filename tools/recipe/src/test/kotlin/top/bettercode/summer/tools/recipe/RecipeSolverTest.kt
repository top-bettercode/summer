package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.solver.SolverType
import top.bettercode.summer.tools.recipe.data.*
import top.bettercode.summer.tools.recipe.indicator.RecipeIndicator
import top.bettercode.summer.tools.recipe.material.IRecipeMaterial
import top.bettercode.summer.tools.recipe.result.Recipe
import top.bettercode.summer.tools.recipe.result.RecipeResult
import java.io.File


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
        val requirement = PrepareData.readRequirement(productName)
        val maxResult = 20
        val solve = MultiRecipeSolver.solve(solverType = SolverType.COPT, requirement = requirement, maxResult = maxResult)
        val solve1 = MultiRecipeSolver.solve(solverType = SolverType.CBC, requirement = requirement, maxResult = maxResult)
        val solve2 = MultiRecipeSolver.solve(solverType = SolverType.SCIP, requirement = requirement, maxResult = maxResult)
//        toExcel(solve)
//        toExcel(solve1)
//        toExcel(solve2)
        System.err.println("copt:" + solve.time)
        System.err.println("cbc:" + solve1.time)
        System.err.println("scip:" + solve2.time)
//        System.err.println(json(solve.recipes[0], IRecipeMaterial::class.java to RecipeMaterialView::class.java, Recipe::class.java to RecipeView::class.java))

//        validate(solve)
//        validate(solve1)
//        validate(solve2)

        assert(solve, solve1)
        assert(solve, solve2)
        assert(solve1, solve2)

        saveRecipe(solve)
        saveRecipe(solve1)
        saveRecipe(solve2)
    }

    private fun toExcel(recipeResult: RecipeResult) {
        val excel = recipeResult.toExcel()
        if (excel != null) {
            Runtime.getRuntime().exec(arrayOf("xdg-open", excel.absolutePath))
        }
        System.err.println("==================================================")
        System.err.println(" 耗时：" + recipeResult.time + "ms" + " 结果：" + recipeResult.recipes.size + "个")
        System.err.println("==================================================")
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
        Assertions.assertEquals(expectedRequirement, json(requirement,
                IRecipeMaterial::class.java to RecipeMaterialView::class.java,
                RecipeRequirement::class.java to RecipeRequirementView::class.java,
                RecipeIndicator::class.java to RecipeIndicatorView::class.java), recipeResult.solverName)
        //配方
        val dir = "recipe/$productName/${recipeResult.solverName}"
        recipes.forEachIndexed { index, recipe ->
            val expectedRecipe = RecipeResult::class.java.getResourceAsStream("/$dir/配方${index + 1}.json")!!.bufferedReader().readText()
            Assertions.assertEquals(expectedRecipe, json(recipe, IRecipeMaterial::class.java to RecipeMaterialView::class.java, Recipe::class.java to RecipeView::class.java), recipeResult.solverName)
        }
    }

    private fun assert(solve: RecipeResult, solve1: RecipeResult) {
        Assertions.assertEquals(solve.recipes[0].cost.scale(7), solve1.recipes[0].cost.scale(7))
        Assertions.assertEquals(solve.recipes.size, solve1.recipes.size)
        Assertions.assertEquals(json(solve.recipes[0].materials), json(solve1.recipes[0].materials))
    }

    private fun json(value: Any, vararg view: Pair<Class<*>, Class<*>>): String {
        val objectMapper = ObjectMapper()
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        val simpleModule = SimpleModule()
        view.forEach { (targetType, mixinClass) ->
            simpleModule.setMixInAnnotation(targetType, mixinClass)
        }
        objectMapper.registerModule(simpleModule)
        return objectMapper.writeValueAsString(value)
    }

    private fun saveRecipe(recipeResult: RecipeResult) {
        val recipes = recipeResult.recipes
        val requirement = recipes[0].requirement
        val productName = requirement.productName
        val dir = File("${System.getProperty("user.dir")}/src/test/resources/recipe/$productName")
        dir.mkdirs()
        //配方要求
        File("$dir/requirement.json").writeText(json(requirement,
                IRecipeMaterial::class.java to RecipeMaterialView::class.java,
                RecipeRequirement::class.java to RecipeRequirementView::class.java,
                RecipeIndicator::class.java to RecipeIndicatorView::class.java))
        //配方
        recipes.forEachIndexed { index, recipe ->
            File("$dir/${recipeResult.solverName}/配方${index + 1}.json").writeText(json(recipe, IRecipeMaterial::class.java to RecipeMaterialView::class.java, Recipe::class.java to RecipeView::class.java))
        }
    }
}