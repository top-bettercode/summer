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

        /**
         * 增减比率
         */
        var change: Double = 1.0
) {

    val cost: Double = value * price
}