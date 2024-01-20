package top.bettercode.summer.tools.recipe.indicator

/**
 *
 * @author Peter Wu
 */
open class RecipeIndicators<T>(indicators: List<RecipeIndicator<T>>) : HashMap<Int, RecipeIndicator<T>>() {
    init {
        indicators.forEach {
            put(it.index, it)
        }
    }

    val nutrients = indicators.filter { it.isNutrient }

    val water = indicators.find { it.type == RecipeIndicatorType.WATER }

    @JvmName("mutableIterator")
    operator fun iterator(): MutableIterator<RecipeIndicator<T>> = values.iterator()
}
