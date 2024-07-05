package top.bettercode.summer.tools.recipe.indicator

import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import kotlin.properties.Delegates

/**
 *
 * @author Peter Wu
 */
class RecipeValueIndicators(indicators: List<RecipeIndicatorValue<Double>> = emptyList()) :
    RecipeIndicatorValues<Double>(indicators) {

    fun init(indicators: RecipeIndicators) {
        this.values.forEach {
            it.indicator = indicators[it.id] ?: throw IllegalArgumentException("未知指标:${it.id}")
            it.scaledValue = it.indicator.scaleOf(it.value)
        }
        key = this.values.sortedBy { indicators[it.id]!!.index }
            .joinToString(",") { "${it.id}:${it.value.scale()}" }

        val water = indicators.water
        waterValue = if (water == null) {
            0.0
        } else {
            this[water.id]?.scaledValue ?: 0.0
        }
        nutrients = indicators.nutrients.mapNotNull { this[it.id] }
    }

    lateinit var key: String

    var waterValue by Delegates.notNull<Double>()

    lateinit var nutrients: List<RecipeIndicatorValue<Double>>

    fun valueOf(id: String): Double {
        return this[id]?.scaledValue ?: 0.0
    }

    companion object {
        val EMPTY = RecipeValueIndicators(emptyList())
    }
}