package top.bettercode.summer.tools.recipe.indicator

/**
 *
 * @author Peter Wu
 */
open class RecipeIndicators(indicators: List<RecipeIndicator>) : HashMap<String, RecipeIndicator>() {
    init {
        indicators.forEach {
            put(it.id, it)
        }
    }

    /**
     * 养分指标
     */
    val nutrients by lazy { values.filter { it.isNutrient } }

    /**
     * 水分指标
     */
    val water by lazy { values.find { it.type == RecipeIndicatorType.WATER } }

    /**
     * 产品水分指标
     */
    val productWater by lazy { values.find { it.type == RecipeIndicatorType.PRODUCT_WATER } }

    @JvmName("mutableIterator")
    operator fun iterator(): MutableIterator<RecipeIndicator> = values.iterator()
}
