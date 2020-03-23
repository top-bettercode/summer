package cn.bestwu.autodoc.core.postman

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("listen", "script")
data class Event(@JsonProperty("listen")
                 var listen: String? = null,
                 @JsonProperty("script")
                 var script: Script? = null)