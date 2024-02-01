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
        /**
         * 其他物料消耗详情,key:物料ID,value: Pair first:正常消耗，second:过量消耗
         */
        @get:JsonIgnore
        val consumes: Map<String, Pair<Double, Double>>
) : IRecipeMaterial by material {

    val normalWeight: Double = consumes.values.sumOf { it.first }
    val overdoseWeight: Double = consumes.values.sumOf { it.second }

    fun normalWeight(ids: MaterialIDs?): Double {
        return if (ids == null) {
            normalWeight
        } else {
            consumes.filter { ids.contains(it.key) }.values.sumOf { it.first }
        }
    }

    fun overdoseWeight(ids: MaterialIDs?): Double {
        return if (ids == null) {
            overdoseWeight
        } else {
            consumes.filter { ids.contains(it.key) }.values.sumOf { it.second }
        }
    }

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