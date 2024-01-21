package top.bettercode.summer.tools.recipe.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators
import top.bettercode.summer.tools.recipe.material.RecipeMaterialType

/**
 * 原料
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
interface RecipeMaterialView {
    /**
     * 序号，从0开始
     */
    val index: Int

    /** 原料ID  */
    val id: String

    /** 原料名称  */
    val name: String

    /** 原料价格  */
    val price: Double

    /**
     * 原料指标
     */
    @get:JsonIgnore
    val indicators: RecipeValueIndicators

    /**
     * 类型
     */
    val type: RecipeMaterialType

}

@JsonPropertyOrder(alphabetic = true)
interface RecipeView {
    @get:JsonIgnore
    val requirement: RecipeRequirement
}
