package top.bettercode.summer.tools.recipe

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class CarrierValue<T, V>(
        @JsonProperty("it")
        val it: T,
        @JsonProperty("value")
        var value: V
)