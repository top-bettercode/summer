package top.bettercode.summer.tools.autodoc.postman


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Item(
        @JsonProperty("_postman_id")
        var postmanId: String? = UUID.randomUUID().toString(),
        @JsonProperty("name")
        var name: String? = null,
        @JsonProperty("description")
        var description: String? = "",
        @JsonProperty("item")
        var item: List<Item>? = null,
        @JsonProperty("event")
        var event: List<Event>? = null,
        @JsonProperty("request")
        var request: Request? = null,
        @JsonProperty("response")
        var response: List<Response>? = null
)