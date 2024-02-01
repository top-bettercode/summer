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
        /**
         * 其他原料消耗详情,key:原料ID,value: Pair first:正常消耗，second:过量消耗
         */
        val consumes: MutableMap<String, Pair<IVar, IVar>> = mutableMapOf()
) : IRecipeMaterial by material {

    fun totalNutrient(): Double {
        return indicators.nutrients.sumOf { indicators.valueOf(it.id) }
    }

    fun toMaterialValue(): RecipeMaterialValue {
        return RecipeMaterialValue(
                material, weight.value,
                consumes.mapValues { it.value.first.value to it.value.second.value }
        )
    }
}