package top.bettercode.summer.tools.recipe.criteria

data class DoubleRange(val min: Double, val max: Double) {
    override fun toString(): String {
        return "$min - $max"
    }
}
