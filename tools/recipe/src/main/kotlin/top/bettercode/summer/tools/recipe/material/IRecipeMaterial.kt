package top.bettercode.summer.tools.recipe.material

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 原料
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
interface IRecipeMaterial : Comparable<IRecipeMaterial> {
    /**
     * 序号，从0开始
     */
    val index: Int

    /** 原料ID  */
    val id: String

    /** 原料名称  */
    val name: String

    /**
     * 单位
     */
    val unit: String

    /** 原料价格  */
    val price: Double

    /**
     * 原料指标
     */
    val indicators: RecipeValueIndicators

    override fun compareTo(other: IRecipeMaterial): Int {
        return index.compareTo(other.index)
    }

    @get:JsonIgnore
    val totalNutrient: Double
        get() {
            return indicators.nutrients.sumOf { it.scaledValue }
        }
}


