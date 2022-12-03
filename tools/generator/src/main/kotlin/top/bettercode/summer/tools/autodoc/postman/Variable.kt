package top.bettercode.summer.tools.autodoc.postman

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("id", "key", "value", "type", "description")
class Variable(@JsonProperty("id")
                    var id: String? = UUID.randomUUID().toString(),
                    @JsonProperty("key")
                    var key: String? = null,
                    @JsonProperty("value")
                    var value: String? = null,
                    @JsonProperty("type")
                    var type: String? = null,
                    @JsonProperty("description")
                    var description: String? = null){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Variable) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key?.hashCode() ?: 0
    }
}