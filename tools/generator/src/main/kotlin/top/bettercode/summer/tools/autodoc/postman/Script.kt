package top.bettercode.summer.tools.autodoc.postman

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("id", "type", "exec")
data class Script(
    @JsonProperty("id")
    var id: String? = UUID.randomUUID().toString(),
    @JsonProperty("type")
    var type: String? = "text/javascript",
    @JsonProperty("exec")
    var exec: List<String>? = null
)