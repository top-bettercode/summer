package top.bettercode.summer.tools.recipe.criteria

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(alphabetic = true)
data class Usage(
        @JsonProperty("normal")
        val normal: Double,
        @JsonProperty("overdose")
        val overdose: Double)