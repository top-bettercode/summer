package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.optimal.solver.`var`.IVar

/**
 *
 * @author Peter Wu
 */
data class RecipeMaterialVar(
        private val material: IRecipeMaterial,
        /** 最终使用量  */
        val weight: IVar,
        var normalWeight: IVar? = null,
        var overdoseWeight: IVar? = null
) : IRecipeMaterial by material {

    fun totalNutrient(): Double {
        return indicators.nutrients.sumOf { indicators.valueOf(it.id) }
    }

    fun toMaterialValue(): RecipeMaterialValue {
        return RecipeMaterialValue(
                material, weight.value,
                normalWeight?.value ?: 0.0,
                overdoseWeight?.value ?: 0.0,
        )
    }
}