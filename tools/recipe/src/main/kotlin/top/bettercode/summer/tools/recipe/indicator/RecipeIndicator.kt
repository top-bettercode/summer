package top.bettercode.summer.tools.recipe.indicator

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.criteria.DoubleRange


/**
 * 指标
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class RecipeIndicator(
    /**
     * 序号，从0开始
     */
    @JsonProperty("index")
    val index: Int,
    /**
     * ID
     */
    @JsonProperty("id")
    val id: String,
    /**
     * 名称
     */
    @JsonProperty("name")
    val name: String,
    /**
     * 单位
     */
    @JsonProperty("unit")
    val unit: String? = null,
    /**
     * 类型
     */
    @JsonProperty("type")
    val type: RecipeIndicatorType = RecipeIndicatorType.GENERAL,
    /**
     * type为RATE_TO_OTHER时，itself指标 ID
     */
    @JsonProperty("itId")
    val itId: String? = null,
    /**
     * type为RATE_TO_OTHER时，other指标 ID
     */
    @JsonProperty("otherId")
    val otherId: String? = null
) : Comparable<RecipeIndicator> {

    @get:JsonIgnore
    val isTotalNutrient = type == RecipeIndicatorType.TOTAL_NUTRIENT

    @get:JsonIgnore
    val isNutrient = type == RecipeIndicatorType.NUTRIENT

    @get:JsonIgnore
    val isProductWater = type == RecipeIndicatorType.PRODUCT_WATER

    @get:JsonIgnore
    val isWater = type == RecipeIndicatorType.WATER

    @get:JsonIgnore
    val isRateToOther = type == RecipeIndicatorType.RATE_TO_OTHER

    /**
     * 单位换算比值
     */
    @get:JsonIgnore
    val scale: Double = if (unit.isNullOrBlank())
        1.0
    else
        when (IndicatorUnit.enumOf(unit)) {
            IndicatorUnit.PERCENTAGE -> 0.01
            IndicatorUnit.KG_TON -> 0.001
            IndicatorUnit.BILLION -> 1.0
        }

    /**
     * 换算值
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> scaleOf(value: T) = when (value) {
        is Double -> (value as Double * scale) as T
        is DoubleRange -> (value as DoubleRange).replaceRate(scale) as T
        else -> value
    }

    override fun compareTo(other: RecipeIndicator): Int {
        return id.compareTo(other.id)
    }
}