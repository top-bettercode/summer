package top.bettercode.summer.tools.recipe.indicator

import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
class RecipeValueIndicators(indicators: List<RecipeIndicator<Double>>) : RecipeIndicators<Double>(indicators) {

    val key: String by lazy {
        values.filter { it.type != RecipeIndicatorType.WATER }.joinToString(",") { it.value.scale().toString() }
    }

    val waterValue: Double by lazy {
        if (water == null) {
            0.0
        } else {
            valueOf(water.id)
        }
    }

    fun valueOf(index: Int): Double {
        return get(index)?.value ?: 0.0
    }

}