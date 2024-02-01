package top.bettercode.summer.tools.recipe.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.indicator.RecipeValueIndicators

/**
 * 原料
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
interface RecipeMaterialView {
    @get:JsonIgnore
    val indicators: RecipeValueIndicators
}

@JsonPropertyOrder(alphabetic = true)
interface RecipeView {
    @get:JsonIgnore
    val requirement: RecipeRequirement
}

@JsonPropertyOrder(alphabetic = true)
interface RecipeRequirementView

@JsonPropertyOrder(alphabetic = true)
interface RecipeIndicatorView