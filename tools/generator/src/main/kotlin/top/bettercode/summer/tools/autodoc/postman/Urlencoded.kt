package top.bettercode.summer.tools.autodoc.postman


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Urlencoded(
        @JsonProperty("key")
        var key: String? = null,
        @JsonProperty("value")
        var value: String? = null,
        @JsonProperty("type")
        var type: String? = null,
        @JsonProperty("description")
        var description: String? = null,
        @JsonProperty("sessionValue")
        var sessionValue: String? = null
)