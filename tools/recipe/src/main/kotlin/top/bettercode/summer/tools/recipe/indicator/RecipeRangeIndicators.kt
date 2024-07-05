package top.bettercode.summer.tools.recipe.indicator

import top.bettercode.summer.tools.recipe.criteria.DoubleRange

/**
 *
 * @author Peter Wu
 */
class RecipeRangeIndicators(indicators: List<RecipeIndicatorValue<DoubleRange>> = emptyList()) :
    RecipeIndicatorValues<DoubleRange>(indicators) {

    fun init(indicators: RecipeIndicators) {
        this.values.forEach {
            it.indicator = indicators[it.id] ?: throw IllegalArgumentException("未知指标:${it.id}")
            it.scaledValue = it.indicator.scaleOf(it.value)
        }
        productWaterValue =
            indicators.productWater?.run { this@RecipeRangeIndicators[id]?.scaledValue }
    }

    var productWaterValue: DoubleRange? = null
}