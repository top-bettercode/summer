package top.bettercode.summer.tools.recipe.criteria

/**
 *
 * @author Peter Wu
 */
data class RecipeRelation(var normal: DoubleRange,
                          var overdose: DoubleRange? = null
) {
    override fun toString(): String {
        return "normal:$normal, overdose:$overdose"
    }
}