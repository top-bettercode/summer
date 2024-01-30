package top.bettercode.summer.tools.recipe.material

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 *
 * @author Peter Wu
 */
data class RecipeMaterialValue(
        private val material: IRecipeMaterial,
        /** 最终使用量  */
        val weight: Double,
        val normalWeight: Double,
        val overdoseWeight: Double,
        ) : IRecipeMaterial by material {

    /**
     * 成本
     */
    @get:JsonIgnore
    val cost: Double by lazy {
        weight * price
    }

    @get:JsonIgnore
    val waterWeight: Double by lazy {
        val water = indicators.water
        if (water != null) {
            indicatorWeight(water.id)
        } else {
            0.0
        }
    }

    fun indicatorWeight(id: String): Double {
        return indicators.valueOf(id) * weight
    }

    fun totalNutrient(): Double {
        return indicators.nutrients.sumOf { indicatorWeight(it.id) }
    }

}