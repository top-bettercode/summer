package top.bettercode.summer.tools.autodoc.postman


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Query(
        @JsonProperty("key")
        var key: String? = null,
        @JsonProperty("value")
        var value: String? = null,
        @JsonProperty("description")
        var description: String? = null
)