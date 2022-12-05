package top.bettercode.summer.tools.autodoc.postman


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class HeaderItem(
    @JsonProperty("key")
    var key: String? = null,
    @JsonProperty("name")
    var name: String? = null,
    @JsonProperty("value")
    var value: String? = null,
    @JsonProperty("description")
    var description: String? = null
)