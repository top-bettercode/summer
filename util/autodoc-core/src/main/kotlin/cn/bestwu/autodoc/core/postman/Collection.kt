package cn.bestwu.autodoc.core.postman


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Collection(
        @JsonProperty("item")
        var item: List<Item>? = null,
        @JsonProperty("info")
        var info: Info? = null,
        @JsonProperty("event")
        var event: List<Event>? = null,
        @JsonProperty("variable")
        var variable: List<Variable>? = null
)