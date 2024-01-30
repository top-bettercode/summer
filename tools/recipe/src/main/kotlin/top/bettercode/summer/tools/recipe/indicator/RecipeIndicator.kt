package top.bettercode.summer.tools.recipe.indicator


/**
 * 指标
 * @author Peter Wu
 */
data class RecipeIndicator<T>(
        /**
         * 序号，从0开始
         */
        val index:Int,
        /**
         * ID
         */
        val id: String,
        /**
         * 名称
         */
        val name: String,
        /**
         * 值
         */
        var value: T,
        /**
         * 类型
         */
        val type: RecipeIndicatorType = RecipeIndicatorType.GENERAL,
        /**
         * type为RATE_TO_OTHER时，itself指标 ID
         */
        val itId: String? = null,
        /**
         * type为RATE_TO_OTHER时，other指标 ID
         */
        val otherId: String? = null
):Comparable<RecipeIndicator<T>> {

    val isNutrient = type == RecipeIndicatorType.NUTRIENT

    val isProductWater = type == RecipeIndicatorType.PRODUCT_WATER

    val isWater = type == RecipeIndicatorType.WATER

    val isRateToOther = type == RecipeIndicatorType.RATE_TO_OTHER

    override fun compareTo(other: RecipeIndicator<T>): Int {
        return id .compareTo(other.id)
    }
}