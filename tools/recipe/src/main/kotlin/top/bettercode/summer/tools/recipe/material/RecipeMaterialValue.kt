package top.bettercode.summer.tools.recipe.material

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.criteria.Usage
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs

/**
 *
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
data class RecipeMaterialValue(
    @JsonProperty("material")
    val material: RecipeMaterial,
    /** 最终使用量  */
    @JsonProperty("weight")
    val weight: Double,
    /**
     * 其他原料消耗详情,key:原料ID
     */
    @JsonProperty("consumes")
    val consumes: Map<String, Usage>
) : IRecipeMaterial by material {

    val normalWeight: Double = consumes.values.sumOf { it.normal }
    val overdoseWeight: Double = consumes.values.sumOf { it.overdose }

    fun normalWeight(ids: MaterialIDs?): Double {
        return if (ids == null) {
            normalWeight
        } else {
            consumes.filter { ids.contains(it.key) }.values.sumOf { it.normal }
        }
    }

    fun overdoseWeight(ids: MaterialIDs?): Double {
        return if (ids == null) {
            overdoseWeight
        } else {
            consumes.filter { ids.contains(it.key) }.values.sumOf { it.overdose }
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
        indicators.waterValue * weight
    }

    @get:JsonIgnore
    val totalNutrientWeight: Double by lazy {
        indicators.nutrients.sumOf { indicatorWeight(it.id) }
    }

    fun indicatorWeight(id: String): Double {
        return (indicators[id]?.scaledValue ?: 0.0) * weight
    }

}