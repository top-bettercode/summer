package top.bettercode.summer.tools.recipe.criteria

import top.bettercode.summer.tools.optimal.solver.Sense

/**
 * 数学约束
 * @author Peter Wu
 */
data class RecipeCondition(
        /**
         * 符号
         */
        val sense: Sense = Sense.EQ,

        /**
         * 值
         */
        val value: Double
) {
    override fun toString(): String {
        return "$sense $value"
    }
}
