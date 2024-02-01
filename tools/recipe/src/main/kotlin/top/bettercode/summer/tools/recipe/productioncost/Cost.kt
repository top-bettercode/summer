package top.bettercode.summer.tools.recipe.productioncost

/**
 *
 * @author Peter Wu
 */
data class Cost(
        /**
         * 数量
         */
        val value: Double,
        /**
         * 价格
         */
        val price: Double,
) {
    val cost = price * value
}