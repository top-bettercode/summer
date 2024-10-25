package top.bettercode.summer.tools.autodoc.postman


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Request(
        @JsonProperty("method")
        var method: String? = null,
        @JsonProperty("header")
        var header: List<HeaderItem>? = null,
        @JsonProperty("body")
        var body: Body? = null,
        @JsonProperty("url")
        var url: Url? = null,
        @JsonProperty("description")
        var description: String? = null
)