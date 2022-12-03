package top.bettercode.summer.tools.autodoc.postman

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("key", "value")
data class Header(@JsonProperty("key")
             val key: String? = null,
             @JsonProperty("value")
             val value: String? = null)