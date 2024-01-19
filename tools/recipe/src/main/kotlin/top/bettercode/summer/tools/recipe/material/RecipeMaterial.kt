package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 原料
 *
 * @author Peter Wu
 */
class RecipeMaterial(
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
        /**
         * 原料指标
         */
        override val indicators: RecipeValueIndicators,
        /**
         * 类型
         */
        override val type: RecipeMaterialType = RecipeMaterialType.GENERAL) : IRecipeMaterial {


    /**
     * 是否是硫酸原料
     */
    val isSulfuricAcid: Boolean = type == RecipeMaterialType.SULFURIC_ACID

    /**
     * 是否是液氨原料
     */
    val isLiquidAmmonia: Boolean = type == RecipeMaterialType.LIQUID_AMMONIA

    /**
     * 是否是碳铵原料
     */
    val isAmmmoniumCarbonate: Boolean = type == RecipeMaterialType.AMMONIUM_CARBONATE


}
