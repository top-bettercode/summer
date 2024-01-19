package top.bettercode.summer.tools.recipe.material

import top.bettercode.summer.tools.recipe.criteria.RecipeCondition

/**
 *
 * @author Peter Wu
 */
data class MaterialCondition(var materials: MaterialIDs, val condition: RecipeCondition) {

    override fun toString(): String {
        return "$materials $condition"
    }

}

