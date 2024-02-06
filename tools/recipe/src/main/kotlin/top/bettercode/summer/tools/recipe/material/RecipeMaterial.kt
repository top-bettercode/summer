package top.bettercode.summer.tools.recipe.material

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 原料
 *
 * @author Peter Wu
 */
data class RecipeMaterial(
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
        /**
         * 原料指标
         */
        @JsonProperty("indicators")
        override val indicators: RecipeValueIndicators
) : IRecipeMaterial
