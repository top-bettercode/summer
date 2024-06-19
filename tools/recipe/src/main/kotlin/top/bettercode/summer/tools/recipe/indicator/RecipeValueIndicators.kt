package top.bettercode.summer.tools.recipe.indicator

import top.bettercode.summer.tools.optimal.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class RecipeValueIndicators(indicators: List<RecipeIndicator<Double>> = emptyList()) :
    RecipeIndicators<Double>(indicators) {


    val key: String by lazy {
        values.sortedBy { it.index }.joinToString(",") { "${it.id}:${it.value.scale()}" }
    }

    val waterValue: Double by lazy {
        val water = this.water
        if (water == null) {
            0.0
        } else {
            valueOf(water.id)
        }
    }

    fun valueOf(id: String): Double {
        return get(id)?.scaledValue ?: 0.0
    }

    companion object {
        val EMPTY = RecipeValueIndicators(emptyList())
    }
}