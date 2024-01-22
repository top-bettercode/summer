package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 其他原料
 * @author Peter Wu
 */
class RecipeOtherMaterial(
        /**
         * 序号，从0开始
         */
        override val index: Int,
        /** 原料ID  */
        override val id: String,
        /** 原料名称  */
        override val name: String,
        /** 原料价格  */
        override val price: Double,
        /**包装耗材数量  */
        val materialQuantity: Double,
) : IRecipeMaterial {
    /**
     * 原料指标
     */
    override val indicators: RecipeValueIndicators = RecipeValueIndicators(emptyList())
}