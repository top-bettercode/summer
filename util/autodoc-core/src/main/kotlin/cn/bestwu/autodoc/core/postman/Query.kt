package cn.bestwu.autodoc.core.postman


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Query(
        @JsonProperty("key")
        var key: String? = null,
        @JsonProperty("value")
        var value: String? = null,
        @JsonProperty("description")
        var description: String? = null
)