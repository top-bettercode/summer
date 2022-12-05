package top.bettercode.summer.tools.autodoc.postman


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Url(

    @JsonProperty("path")
    var path: List<String>? = null,
    @JsonProperty("protocol")
    var protocol: String? = null,
    @JsonProperty("port")
    var port: String? = null,
    @JsonProperty("host")
    var host: List<String>? = null,
    @JsonProperty("raw")
    var raw: String? = null,
    @JsonProperty("query")
    var query: List<Query>? = null
)