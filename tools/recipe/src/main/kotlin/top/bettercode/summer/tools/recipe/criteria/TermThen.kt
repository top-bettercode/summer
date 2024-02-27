package top.bettercode.summer.tools.recipe.criteria

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class TermThen<T, E>(
        @JsonProperty("term")
        var term: T,
        @JsonProperty("then")
        var then: E
)