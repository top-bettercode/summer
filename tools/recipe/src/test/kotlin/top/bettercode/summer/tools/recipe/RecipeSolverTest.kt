package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.excel.FastExcel
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.solver.SolverType
import top.bettercode.summer.tools.recipe.data.RecipeMaterialView
import top.bettercode.summer.tools.recipe.data.RecipeView
import top.bettercode.summer.tools.recipe.data.TestPrepareData
import top.bettercode.summer.tools.recipe.material.IRecipeMaterial
import top.bettercode.summer.tools.recipe.result.Recipe
import top.bettercode.summer.tools.recipe.result.RecipeResult
import java.io.File


/**
 * @author Peter Wu
 */
internal class RecipeSolverTest {
    val products = listOf(
            "13-05-07高氯枸磷",
            "24-06-10高氯枸磷",
            "15-15-15喷浆硫基",
            "15-15-15喷浆氯基",
            "15-15-15常规氯基"
    )
    val solveTypes = listOf(
            SolverType.COPT,
            SolverType.CPLEX,
            SolverType.SCIP,
            SolverType.GUROBI,
            SolverType.CBC,
    )

    val maxResult = 20
    val includeProductionCost = true
    val nutrientUnchanged = true
    val materialUnchanged = true
    val solveTimes = 1
    val toExcel = false
    val epsilon = 1e-4

    @Test
    fun solve() {
        val results = solveList(solveTimes)
        val file = File("build/excel/time-${System.currentTimeMillis()}.xlsx")
        file.parentFile.mkdirs()
        FastExcel.of(file).apply {
            sheet("sheet1")
            var r = 0
            var c = 0
            cell(r++, c).value("产品").headerStyle().width(20.0).setStyle()
            results.forEach { (product, value) ->
                c = 0
                cell(r, c).value(product).headerStyle().setStyle()
                value.forEach { (solverType, time) ->
                    c++
                    if (r == 1)
                        cell(0, c).value(solverType.name).headerStyle().setStyle()
                    cell(r, c).value(time).setStyle()
                }
                r++
            }
            finish()
        }
        Runtime.getRuntime().exec(arrayOf("xdg-open", file.absolutePath))
    }

    private fun solve(requirement: RecipeRequirement, solverType: SolverType = SolverType.COPT, maxResult: Int, includeProductionCost: Boolean, nutrientUnchanged: Boolean, materialUnchanged: Boolean, toExcel: Boolean = false): RecipeResult {
        val recipeResult = MultiRecipeSolver.solve(solverType = solverType, requirement = requirement, maxResult = maxResult, includeProductionCost = includeProductionCost, nutrientUnchanged = nutrientUnchanged, materialUnchanged = materialUnchanged, epsilon = epsilon)

        System.err.println("============toExcel=============")
        if (toExcel)
            toExcel(recipeResult)

        System.err.println("============效验结果=============")
        validateResult(recipeResult)

        System.err.println("============对比保存结果=============")
        validatePreResult(recipeResult)

        System.err.println("============保存结果=============")
        saveRecipe(recipeResult)
        return recipeResult
    }


    fun solve(productName: String): Map<SolverType, Long> {
        System.err.println("======================$productName=====================")
        var requirement = TestPrepareData.readRequirement(productName)
        val file = File("build/requirement.json")
        requirement.write(file)
        requirement = RecipeRequirement.read(file)

        var recipeResult: RecipeResult? = null

        val results = mutableMapOf<SolverType, Long>()
        solveTypes.forEach {
            val result = solve(solverType = it, requirement = requirement, maxResult = maxResult, includeProductionCost = includeProductionCost, nutrientUnchanged = nutrientUnchanged, materialUnchanged = materialUnchanged, toExcel = toExcel)
            recipeResult?.let { res -> assert(res, result) }
            recipeResult = result
            results[it] = result.time
        }

        return results
    }

    fun solveList(times: Int = 1): Map<String, Map<SolverType, Long>> {
        return products.associateWith { product ->
            val solverInfos = mutableMapOf<SolverType, MutableList<Long>>()
            (0 until times).forEach { _ ->
                solve(product).forEach { (solverType, value) ->
                    solverInfos.getOrPut(solverType) { mutableListOf() }.add(value)
                }
            }
            solverInfos.mapValues { it.value.average().toLong() }
        }
    }

    private fun toExcel(recipeResult: RecipeResult) {
        val recipes = recipeResult.recipes
        val size = recipes.size
        if (size == 0) {
            return
        }
        val requirement = recipes[0].requirement
        val fileName: String = (requirement.productName + if (requirement.maxUseMaterialNum == null) "配方计算结果-进料口不限" else "配方计算结果-进料口不大于${requirement.maxUseMaterialNum}")
        val outFile = File("build/excel/" + recipeResult.solverName + "-${fileName}" + "-推" + size + "个-" + System.currentTimeMillis() + ".xlsx")
        outFile.parentFile.mkdirs()

        recipeResult.toExcel(outFile)
        Runtime.getRuntime().exec(arrayOf("xdg-open", outFile.absolutePath))
        System.err.println("==================================================")
        System.err.println(" 耗时：" + recipeResult.time + "ms" + " 结果：" + size + "个")
        System.err.println("==================================================")
    }

    private fun validateResult(recipeResult: RecipeResult) {
        recipeResult.recipes.forEachIndexed { index, recipe ->
            System.err.println("=============${recipeResult.solverName}配方${index + 1}==============")
            Assertions.assertTrue(recipe.validate())
        }
    }

    private fun validatePreResult(recipeResult: RecipeResult) {
        val recipes = recipeResult.recipes
        val requirement = recipes[0].requirement
        val productName = requirement.productName
        val recipe1 = recipes[0]
        val includeProductionCost = recipe1.includeProductionCost
        val dir = File("${System.getProperty("user.dir")}/src/test/resources/recipe${if (includeProductionCost) "-productionCost" else ""}/$productName")
        dir.mkdirs()
        val expectedRequirement = File("$dir/requirement.json").bufferedReader().readText()
        //配方要求
        Assertions.assertEquals(expectedRequirement, json(requirement,
                IRecipeMaterial::class.java to RecipeMaterialView::class.java
        ), recipeResult.solverName)
        //配方
        recipes.forEachIndexed { index, recipe ->
            val file = File("$dir/${recipeResult.solverName}/配方${index + 1}.json")
            if (file.exists()) {
                val expectedRecipe = file.bufferedReader().readText()
                Assertions.assertEquals(expectedRecipe, json(recipe, IRecipeMaterial::class.java to RecipeMaterialView::class.java, Recipe::class.java to RecipeView::class.java), recipeResult.solverName + "配方${index + 1}")
            }
        }
    }

    private fun assert(solve: RecipeResult, solve1: RecipeResult) {
        Assertions.assertEquals(solve.recipes[0].cost.scale(7), solve1.recipes[0].cost.scale(7))
        Assertions.assertEquals(solve.recipes.size, solve1.recipes.size)
        Assertions.assertEquals(json(solve.recipes[0].materials), json(solve1.recipes[0].materials))
        solve.recipes.forEachIndexed { index, recipe ->
            Assertions.assertEquals(recipe.cost.scale(7), solve1.recipes[index].cost.scale(7))
        }
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
        val recipe1 = recipes[0]
        val includeProductionCost = recipe1.includeProductionCost
        val requirement = recipe1.requirement
        val productName = requirement.productName
        val dir = File("${System.getProperty("user.dir")}/src/test/resources/recipe${if (includeProductionCost) "-productionCost" else ""}/$productName")
        dir.mkdirs()
        //配方要求
        File("$dir/requirement.json").writeText(json(requirement,
                IRecipeMaterial::class.java to RecipeMaterialView::class.java
        ))
        //配方
        recipes.forEachIndexed { index, recipe ->
            val file = File("$dir/${recipeResult.solverName}/配方${index + 1}.json")
            file.parentFile.mkdirs()
            file.writeText(json(recipe, IRecipeMaterial::class.java to RecipeMaterialView::class.java, Recipe::class.java to RecipeView::class.java))
        }
    }
}