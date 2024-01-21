package top.bettercode.summer.tools.recipe.material

/**
 *
 * @author Peter Wu
 */
data class RecipeMaterialVar(
        private val material: IRecipeMaterial,
        /** 最终使用量  */
        val solutionVar: SolutionVar
) : IRecipeMaterial by material {

    fun totalNutrient(): Double {
        return indicators.nutrients.sumOf { indicators.valueOf(it.index) }
    }

    fun toMaterialValue(): RecipeMaterialValue {
        return RecipeMaterialValue(material, solutionVar.solve())
    }
}