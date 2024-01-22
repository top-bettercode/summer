package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 物料
 *
 * @author Peter Wu
 */
interface IRecipeMaterial : Comparable<IRecipeMaterial> {
    /**
     * 序号，从0开始
     */
    val index: Int

    /** 物料ID  */
    val id: String

    /** 物料名称  */
    val name: String

    /** 物料价格  */
    val price: Double

    /**
     * 物料指标
     */
    val indicators: RecipeValueIndicators

    override fun compareTo(other: IRecipeMaterial): Int {
        return index.compareTo(other.index)
    }

}


