package top.bettercode.summer.tools.recipe.indicator

/**
 *
 * @author Peter Wu
 */
open class RecipeIndicatorValues<T : Any>(indicators: List<RecipeIndicatorValue<T>>) :
    HashMap<String, RecipeIndicatorValue<T>>() {

    init {
        indicators.forEach {
            put(it.id, it)
        }
    }

    @JvmName("mutableIterator")
    operator fun iterator(): MutableIterator<RecipeIndicatorValue<T>> = values.iterator()

}