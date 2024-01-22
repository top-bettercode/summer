package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 其他物料
 * @author Peter Wu
 */
class RecipeOtherMaterial(
        /**
         * 序号，从0开始
         */
        override val index: Int,
        /** 物料ID  */
        override val id: String,
        /** 物料名称  */
        override val name: String,
        /** 物料价格  */
        override val price: Double,
        /** 数量  */
        val value: Double,
) : IRecipeMaterial {
    /**
     * 原料指标
     */
    override val indicators: RecipeValueIndicators = RecipeValueIndicators(emptyList())
}