package top.bettercode.summer.tools.recipe.material

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 其他原料
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class RecipeOtherMaterial(
    /**
     * 序号，从0开始
     */
    @JsonProperty("index")
    override val index: Int,
    /** 原料ID  */
    @JsonProperty("id")
    override val id: String,
    /** 原料名称  */
    @JsonProperty("name")
    override val name: String,
    /** 单位  */
    @JsonProperty("unit")
    override val unit: String = "吨",
    /** 原料价格  */
    @JsonProperty("price")
    override val price: Double,
    /** 数量  */
    @JsonProperty("value")
    val value: Double
) : IRecipeMaterial {
    /**
     * 原料指标
     */
    override val indicators: RecipeValueIndicators = RecipeValueIndicators.EMPTY

    val cost = (price * value)
}