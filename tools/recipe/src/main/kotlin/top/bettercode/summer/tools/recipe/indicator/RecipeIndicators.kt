package top.bettercode.summer.tools.recipe.indicator

/**
 *
 * @author Peter Wu
 */
open class RecipeIndicators<T>(indicators: List<RecipeIndicator<T>>) : HashMap<Int, RecipeIndicator<T>>() {
    init {
        indicators.forEach {
            put(it.id, it)
        }
    }

    /**
     * 养分指标
     */
    val nutrients = indicators.filter { it.isNutrient }

    /**
     * 水分指标
     */
    val water = indicators.find { it.type == RecipeIndicatorType.WATER }

    /**
     * 产品水分指标
     */
    val productWater = indicators.find { it.type == RecipeIndicatorType.PRODUCT_WATER }

    @JvmName("mutableIterator")
    operator fun iterator(): MutableIterator<RecipeIndicator<T>> = values.iterator()
}
