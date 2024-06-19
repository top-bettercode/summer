package top.bettercode.summer.tools.recipe.criteria

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.optimal.OptimalUtil.scale

@JsonPropertyOrder(alphabetic = true)
data class DoubleRange(
    @JsonProperty("min")
    val min: Double,
    @JsonProperty("max")
    val max: Double
) {

    fun replaceRate(rate: Double): DoubleRange {
        return DoubleRange((min * rate).scale(), (max * rate).scale())
    }

    override fun toString(): String {
        return "$min - $max"
    }
}
