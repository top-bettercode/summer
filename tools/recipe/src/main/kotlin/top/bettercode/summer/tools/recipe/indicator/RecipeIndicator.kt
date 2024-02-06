package top.bettercode.summer.tools.recipe.indicator

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder


/**
 * 指标
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
data class RecipeIndicator<T>(
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
         * 值
         */
        @JsonProperty("value")
        var value: T,
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
) : Comparable<RecipeIndicator<T>> {

    @get:JsonIgnore
    val isNutrient = type == RecipeIndicatorType.NUTRIENT

    @get:JsonIgnore
    val isProductWater = type == RecipeIndicatorType.PRODUCT_WATER

    @get:JsonIgnore
    val isWater = type == RecipeIndicatorType.WATER

    @get:JsonIgnore
    val isRateToOther = type == RecipeIndicatorType.RATE_TO_OTHER

    override fun compareTo(other: RecipeIndicator<T>): Int {
        return id.compareTo(other.id)
    }
}