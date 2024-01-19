package top.bettercode.summer.tools.recipe.material

/**
 *
 * @author Peter Wu
 */
class RecipeMaterialValue(
        private val material: IRecipeMaterial,
        /** 最终使用量  */
        val solutionValue: SolutionValue
) : IRecipeMaterial by material {

    fun indicatorWeight(index: Int): Double {
        return indicators.valueOf(index) * solutionValue.value
    }

    fun totalNutrient(): Double {
        return indicators.nutrients.sumOf { indicatorWeight(it.index) }
    }

}