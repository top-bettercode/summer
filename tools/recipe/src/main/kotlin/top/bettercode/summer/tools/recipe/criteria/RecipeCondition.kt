package top.bettercode.summer.tools.recipe.criteria

/**
 * 数学约束
 * @author Peter Wu
 */
data class RecipeCondition(
        /**
         * 符号
         */
        val operator: Operator = Operator.EQ,

        /**
         * 值
         */
        val value: Double
) {
    override fun toString(): String {
        return "$operator $value"
    }
}
