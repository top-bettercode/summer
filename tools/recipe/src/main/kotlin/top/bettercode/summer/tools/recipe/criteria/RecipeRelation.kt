package top.bettercode.summer.tools.recipe.criteria

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class RecipeRelation(
        @JsonProperty("normal")
        var normal: DoubleRange,
        @JsonProperty("overdose")
        var overdose: DoubleRange? = null,
        @JsonProperty("overdoseMaterial")
        var overdoseMaterial: RecipeRelation? = null,
) {
    fun replaceRate(rate: Double): RecipeRelation {
        return RecipeRelation(normal.replaceRate(rate), overdose?.replaceRate(rate), overdoseMaterial?.replaceRate(rate))
    }

    override fun toString(): String {
        return "normal:$normal, overdose:$overdose, overdoseMaterial:$overdoseMaterial"
    }
}