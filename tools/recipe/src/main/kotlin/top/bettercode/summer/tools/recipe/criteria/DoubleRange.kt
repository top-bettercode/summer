package top.bettercode.summer.tools.recipe.criteria

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.optimal.OptimalUtil.scale
import top.bettercode.summer.tools.optimal.Operator

@JsonPropertyOrder(alphabetic = true)
data class DoubleRange(
    @JsonProperty("minSense")
    val minOperator: Operator,
    @JsonProperty("min")
    val min: Double,
    @JsonProperty("maxSense")
    val maxOperator: Operator,
    @JsonProperty("max")
    val max: Double,
) {

    constructor(min: Double, max: Double) : this(
        minOperator = Operator.GE,
        min = min,
        maxOperator = Operator.LE,
        max = max
    )

    fun replaceRate(rate: Double): DoubleRange {
        return DoubleRange(
            min = (min * rate).scale(),
            max = (max * rate).scale(),
            minOperator = minOperator,
            maxOperator = maxOperator
        )
    }

    override fun toString(): String {
        return "$min ${
            when (minOperator) {
                Operator.GE -> Operator.LE
                Operator.GT -> Operator.LT
                else -> minOperator
            }
        } - $maxOperator $max"
    }
}
