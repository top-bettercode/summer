package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 原料
 *
 * @author Peter Wu
 */
interface IRecipeMaterial : Comparable<IRecipeMaterial> {
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
    val indicators: RecipeValueIndicators

    /**
     * 类型
     */
    val type: RecipeMaterialType

    override fun compareTo(other: IRecipeMaterial): Int {
        return index.compareTo(other.index)
    }

}


