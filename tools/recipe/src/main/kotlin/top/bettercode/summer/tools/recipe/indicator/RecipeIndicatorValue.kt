package top.bettercode.summer.tools.recipe.indicator

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import kotlin.properties.Delegates


/**
 * 指标
 * @author Peter Wu
 */
@JsonPropertyOrder(alphabetic = true)
data class RecipeIndicatorValue<T : Any>(
    /**
     * ID
     */
    @JsonProperty("id")
    val id: String,
    /**
     * 值
     */
    @JsonProperty("value")
    var value: T,
) : Comparable<RecipeIndicatorValue<T>> {

    @get:JsonIgnore
    var scaledValue by Delegates.notNull<T>()

    @get:JsonIgnore
    var indicator by Delegates.notNull<RecipeIndicator>()

    override fun compareTo(other: RecipeIndicatorValue<T>): Int {
        return id.compareTo(other.id)
    }
}