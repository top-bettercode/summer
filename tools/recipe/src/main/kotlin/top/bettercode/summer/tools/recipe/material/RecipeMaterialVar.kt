package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.optimal.IVar
import top.bettercode.summer.tools.recipe.criteria.UsageVar

/**
 *
 * @author Peter Wu
 */
data class RecipeMaterialVar(
        private val material: RecipeMaterial,
        /** 最终使用量  */
        val weight: IVar,
        /**
         * 其他原料消耗详情,key:原料ID
         */
        val consumes: MutableMap<String, UsageVar> = mutableMapOf()
) : IRecipeMaterial by material {

    fun totalNutrient(): Double {
        return indicators.nutrients.sumOf { indicators.valueOf(it.id) }
    }

    fun toMaterialValue(): RecipeMaterialValue {
        return RecipeMaterialValue(
                material, weight.value,
                consumes.mapValues { it.value.toUsage() }
        )
    }
}