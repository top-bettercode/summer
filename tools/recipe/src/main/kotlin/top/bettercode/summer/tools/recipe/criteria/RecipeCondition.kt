package top.bettercode.summer.tools.recipe.criteria

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.optimal.solver.Sense

/**
 * 数学约束
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class RecipeCondition(
        /**
         * 符号
         */
        @JsonProperty("sense")
        val sense: Sense = Sense.EQ,

        /**
         * 值
         */
        @JsonProperty("value")
        val value: Double
) {
    override fun toString(): String {
        return "$sense $value"
    }
}
