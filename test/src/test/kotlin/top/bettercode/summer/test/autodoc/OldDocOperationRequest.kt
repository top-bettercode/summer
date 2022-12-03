package top.bettercode.summer.test.autodoc

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.autodoc.Util.singleValueMap
import top.bettercode.summer.tools.autodoc.Util.toMap
import top.bettercode.summer.tools.lang.operation.OperationRequest

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(
    "uri",
    "restUri",
    "uriVariables",
    "method",
    "requiredHeaders",
    "headers",
    "cookies",
    "remoteUser",
    "requiredParameters",
    "parameters",
    "parts",
    "contentAsString"
)
@JsonIgnoreProperties("createdDate")
class OldDocOperationRequest(
    operationRequest: OperationRequest = OperationRequest(),
    /**
     * 请求头必填参数
     */
    var requiredHeaders: Set<String> = setOf(),
    /**
     * 必填参数
     */
    var requiredParameters: Set<String> = setOf()
) : OperationRequest(
    operationRequest.uri,
    operationRequest.restUri,
    operationRequest.uriVariables,
    operationRequest.method,
    operationRequest.headers,
    operationRequest.cookies,
    operationRequest.remoteUser,
    operationRequest.parameters,
    operationRequest.parts.onEach {
        it.content = if (it.submittedFileName.isNullOrBlank()) it.content else ByteArray(0)
    },
    operationRequest.content,
    operationRequest.dateTime
) {

    val docParameters: Map<String, Any?>
        @JsonIgnore
        get() {
            val params = mutableMapOf<String, Any?>()
            params.putAll(parameters.singleValueMap)
            parts.forEach {
                params[it.name] = it.contentAsString
            }
            if (contentAsString.isNotBlank()) {
                val contentMap = contentAsString.toMap()
                if (contentMap != null) {
                    params.putAll(contentMap)
                }
            }
            return params
        }

    @JsonIgnore
    fun needAuth(authVariables: Array<String>): Boolean {
        return requiredHeaders.contains("Authorization") || requiredHeaders.contains("authorization") || requiredHeaders.any {
            authVariables.contains(
                it
            )
        } || requiredParameters.any { authVariables.contains(it) }
    }
}