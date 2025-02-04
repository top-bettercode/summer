package top.bettercode.summer.tools.recipe.material

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.recipe.RecipeRequirement
import top.bettercode.summer.tools.recipe.criteria.RecipeCondition
import top.bettercode.summer.tools.recipe.material.id.MaterialIDs

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class MaterialCondition(
    @JsonProperty("materials")
    val materials: MaterialIDs,
    @JsonProperty("condition")
    val condition: RecipeCondition
) {

    override fun toString(): String {
        return "$materials $condition"
    }

    fun toString(requirement: RecipeRequirement): String {
        return "${materials.toNames(requirement)} $condition"
    }
}

