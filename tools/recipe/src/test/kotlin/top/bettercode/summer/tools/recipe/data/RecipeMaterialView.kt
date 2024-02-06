package top.bettercode.summer.tools.recipe.data

import com.fasterxml.jackson.annotation.JsonIgnore
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 原料
 *
 * @author Peter Wu
 */
interface RecipeMaterialView {
    @get:JsonIgnore
    val indicators: RecipeValueIndicators
}

interface RecipeView {
    @get:JsonIgnore
    val requirement: RecipeRequirement
}

