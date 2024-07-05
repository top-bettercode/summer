package top.bettercode.summer.tools.recipe.indicator

import top.bettercode.summer.tools.recipe.material.id.MaterialIDs

/**
 *
 * @author Peter Wu
 */
class RecipeMaterialIDIndicators(indicators: List<RecipeIndicatorValue<MaterialIDs>> = emptyList()) :
    RecipeIndicatorValues<MaterialIDs>(indicators) {

    fun init(indicators: RecipeIndicators) {
        this.values.forEach {
            it.indicator = indicators[it.id] ?: throw IllegalArgumentException("未知指标:${it.id}")
            it.scaledValue = it.indicator.scaleOf(it.value)
        }
    }

    companion object {
        val EMPTY = RecipeMaterialIDIndicators(emptyList())
    }

}