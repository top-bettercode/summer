package top.bettercode.summer.tools.recipe.indicator


/**
 * 指标
 * @author Peter Wu
 */
data class RecipeIndicator<T>(
        /**
         * 序号，从0开始
         */
        val index: Int,
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
         * type为RATE_TO_OTHER时，itself指标 序号
         */
        val itIndex: Int? = null,
        /**
         * type为RATE_TO_OTHER时，other 序号
         */
        val otherIndex: Int? = null
) {

    val isNutrient = type == RecipeIndicatorType.NUTRIENT

    val isWater = type == RecipeIndicatorType.WATER

    val isRateToOther = type == RecipeIndicatorType.RATE_TO_OTHER
}