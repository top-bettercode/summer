package top.bettercode.summer.tools.recipe.criteria

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.Sense

@JsonPropertyOrder(alphabetic = true)
data class DoubleRange(
    @JsonProperty("minSense")
    val minSense: Sense,
    @JsonProperty("min")
    val min: Double,
    @JsonProperty("maxSense")
    val maxSense: Sense,
    @JsonProperty("max")
    val max: Double,
) {

    constructor(min: Double, max: Double) : this(
        minSense = Sense.GE,
        min = min,
        maxSense = Sense.LE,
        max = max
    )

    fun replaceRate(rate: Double): DoubleRange {
        return DoubleRange(
            min = (min * rate).scale(),
            max = (max * rate).scale(),
            minSense = minSense,
            maxSense = maxSense
        )
    }

    override fun toString(): String {
        return "$min ${
            when (minSense) {
                Sense.GE -> Sense.LE
                Sense.GT -> Sense.LT
                else -> minSense
            }
        } - $maxSense $max"
    }
}
