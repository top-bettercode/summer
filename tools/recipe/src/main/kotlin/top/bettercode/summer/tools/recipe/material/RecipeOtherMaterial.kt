package top.bettercode.summer.tools.recipe.material

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 其他原料
 * @author Peter Wu
 */
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

    val cost = price * value
}