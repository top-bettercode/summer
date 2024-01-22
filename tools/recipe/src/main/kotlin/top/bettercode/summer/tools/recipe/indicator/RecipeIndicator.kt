package top.bettercode.summer.tools.recipe.indicator


/**
 * 指标
 * @author Peter Wu
 */
data class RecipeIndicator<T>(
        /**
         * ID
         */
        val id: Int,
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
        val itId: Int? = null,
        /**
         * type为RATE_TO_OTHER时，other ID
         */
        val otherId: Int? = null
):Comparable<RecipeIndicator<T>> {

    val isNutrient = type == RecipeIndicatorType.NUTRIENT

    val isWater = type == RecipeIndicatorType.WATER

    val isRateToOther = type == RecipeIndicatorType.RATE_TO_OTHER

    override fun compareTo(other: RecipeIndicator<T>): Int {
        return id .compareTo(other.id)
    }
}