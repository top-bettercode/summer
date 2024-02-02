package top.bettercode.summer.tools.recipe.criteria

/**
 *
 * @author Peter Wu
 */
data class RecipeRelation(
        var normal: DoubleRange,
        var overdose: DoubleRange? = null,
        var overdoseMaterial: RecipeRelation? = null,
) {
    fun replaceRate(rate: Double): RecipeRelation {
        return RecipeRelation(normal.replaceRate(rate), overdose?.replaceRate(rate), overdoseMaterial?.replaceRate(rate))
    }

    override fun toString(): String {
        return "normal:$normal, overdose:$overdose, overdoseMaterial:$overdoseMaterial"
    }
}