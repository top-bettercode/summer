package top.bettercode.summer.tools.autodoc.postman


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Response(
        @JsonProperty("name")
        var name: String? = null,
        @JsonProperty("originalRequest")
        var originalRequest: Request? = null,
        @JsonProperty("code")
        var code: Int? = null,
        @JsonProperty("status")
        var status: String? = null,
        @JsonProperty("header")
        var header: List<HeaderItem>? = null,
        @JsonProperty("body")
        var body: String? = null,
        @JsonProperty("id")
        var id: String? = UUID.randomUUID().toString(),
        @JsonProperty("_postman_previewlanguage")
        var postmanPreviewlanguage: String? = null,
        @JsonProperty("cookie")
        var cookie: List<*>? = null
)