package cn.bestwu.autodoc.core.operation

import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.autodoc.core.singleValueMap
import cn.bestwu.autodoc.core.toMap
import cn.bestwu.logging.operation.OperationRequest
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder("parametersExt", "contentExt", "uriVariablesExt", "partsExt", "headersExt", "uri", "restUri", "uriVariables", "method", "headers", "cookies", "parameters", "parts", "contentAsString")
@JsonIgnoreProperties("createdDate", "remoteUser")
class DocOperationRequest(operationRequest: OperationRequest = OperationRequest(),
                          /**
                           * URI variables说明
                           */
                          var uriVariablesExt: SortedSet<Field> = TreeSet(),
                          /**
                           * 请求头说明
                           */
                          var headersExt: SortedSet<Field> = TreeSet(),
                          /**
                           * form参数说明
                           */
                          var parametersExt: SortedSet<Field> = TreeSet(),
                          /**
                           * parts参数说明
                           */
                          var partsExt: SortedSet<Field> = TreeSet(),
                          /**
                           * 请求体参数说明
                           */
                          var contentExt: SortedSet<Field> = TreeSet()
) : OperationRequest(operationRequest.uri, operationRequest.restUri, operationRequest.uriVariables, operationRequest.method, operationRequest.headers, operationRequest.cookies, operationRequest.remoteUser, operationRequest.parameters, operationRequest.parts.apply {
    forEach {
        it.content = if (it.submittedFileName.isNullOrBlank()) it.content else ByteArray(0)
    }
}, operationRequest.content, operationRequest.createdDate) {

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
        return headersExt.any { it.required && it.name.equals("Authorization", true) } || headersExt.any { it.required && authVariables.contains(it.name) } || parametersExt.any { it.required && authVariables.contains(it.name) }
    }
}