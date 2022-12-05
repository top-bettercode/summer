package top.bettercode.summer.tools.autodoc.postman

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("key", "value", "description", "type")
data class Formdatum(
    @JsonProperty("key")
    var key: String? = null,
    @JsonProperty("value")
    var value: String? = null,
    @JsonProperty("type")
    var type: String? = null,
    @JsonProperty("description")
    var description: String? = null
)