package top.bettercode.summer.tools.recipe.criteria

data class DoubleRange(val min: Double, val max: Double) {

    fun replaceRate(rate: Double): DoubleRange {
        return DoubleRange(min * rate, max * rate)
    }

    override fun toString(): String {
        return "$min - $max"
    }
}
