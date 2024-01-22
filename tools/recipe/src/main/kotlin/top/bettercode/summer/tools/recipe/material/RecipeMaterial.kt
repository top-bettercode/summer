package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 物料
 *
 * @author Peter Wu
 */
class RecipeMaterial(
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
        /**
         * 物料指标
         */
        override val indicators: RecipeValueIndicators
) : IRecipeMaterial
