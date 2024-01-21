package top.bettercode.summer.tools.recipe.material

/**
 *
 * @author Peter Wu
 */
data class RecipeMaterialValue(
        private val material: IRecipeMaterial,
        /** 最终使用量  */
        val solutionValue: SolutionValue
) : IRecipeMaterial by material {

    val weight: Double by lazy { solutionValue.value }
    val normalWeight: Double by lazy { solutionValue.normal ?: 0.0 }
    val overdoseWeight: Double by lazy { solutionValue.overdose ?: 0.0 }

    /**
     * 成本
     */
    val cost: Double by lazy {
        weight * price
    }

    val waterWeight: Double by lazy {
        val water = indicators.water
        if (water != null) {
            indicatorWeight(water.index)
        } else {
            0.0
        }
    }

    fun indicatorWeight(index: Int): Double {
        return indicators.valueOf(index) * weight
    }

    fun totalNutrient(): Double {
        return indicators.nutrients.sumOf { indicatorWeight(it.index) }
    }

}