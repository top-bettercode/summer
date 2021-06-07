package top.bettercode.autodoc.core.postman


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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