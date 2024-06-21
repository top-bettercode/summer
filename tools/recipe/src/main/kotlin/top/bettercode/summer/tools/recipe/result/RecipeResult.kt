package top.bettercode.summer.tools.recipe.result

import top.bettercode.summer.tools.excel.FastExcel
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.result.RecipeExport.exportMaterial
import top.bettercode.summer.tools.recipe.result.RecipeExport.exportProductionCost
import top.bettercode.summer.tools.recipe.result.RecipeExport.exportRecipe
import top.bettercode.summer.tools.recipe.result.RecipeExport.exportRequirement
import java.io.File

/**
 * 配方计算结果
 *
 * @author Peter Wu
 */
class RecipeResult @JvmOverloads constructor(
    val requirement: RecipeRequirement,
    /** 配方  */
    var recipes: MutableList<Recipe> = ArrayList(),
    val solverName: String = "defaultSolver"
) {

    /** 耗时  */
    var time: Long = 0

    // --------------------------------------------
    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
    }

    // 输出 Excel
    fun toExcel(outFile: File) {
        val filePath = outFile.absolutePath
        FastExcel.of(filePath).apply {
            sheet("最终候选原料")
            exportMaterial(requirement)
            sheet("配方要求")
            exportRequirement(requirement)

            for ((index, recipe) in recipes.withIndex()) {
                val sheetname = "配方" + (index + 1)
                sheet(sheetname)
                if (index == 0) {
                    keepInActiveTab()
                }
                val row = exportRecipe(recipe, true)
                exportProductionCost(recipe = recipe, row + 1)
            }

            finish()
        }
    }

}
