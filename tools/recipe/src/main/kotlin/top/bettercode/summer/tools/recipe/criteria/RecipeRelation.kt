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
        normal = normal.replaceRate(rate)
        overdose = overdose?.replaceRate(rate)
        overdoseMaterial = overdoseMaterial?.replaceRate(rate)
        return this
    }

    override fun toString(): String {
        return "normal:$normal, overdose:$overdose, overdoseMaterial:$overdoseMaterial"
    }
}