package top.bettercode.summer.tools.recipe.productioncost

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.optimal.solver.OptimalUtil.scale

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class Cost(
        /**
         * 数量
         */
        @JsonProperty("value")
        val value: Double,
        /**
         * 价格
         */
        @JsonProperty("price")
        val price: Double,
) {
    val cost = (price * value).scale()
}