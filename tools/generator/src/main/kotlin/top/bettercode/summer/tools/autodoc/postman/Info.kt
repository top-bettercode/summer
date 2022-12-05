package top.bettercode.summer.tools.autodoc.postman


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Info(

    @JsonProperty("schema")
    var schema: String? = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    @JsonProperty("name")
    var name: String? = null,
    @JsonProperty("description")
    var description: String? = null,
    @JsonProperty("_postman_id")
    var postmanId: String? = UUID.randomUUID().toString()
)