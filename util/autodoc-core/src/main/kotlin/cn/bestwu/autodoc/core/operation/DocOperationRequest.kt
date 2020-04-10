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
@JsonPropertyOrder("parametersExt", "contentExt", "uriVariablesExt", "partsExt", "headersExt", "restUri", "method", "cookies", "contentAsString")
@JsonIgnoreProperties("uri", "uriVariables", "headers", "parameters", "parts", "createdDate", "remoteUser")
class DocOperationRequest(operationRequest: OperationRequest = OperationRequest(),
                          /**
                           * URI variables说明
                           */
                          var uriVariablesExt: LinkedHashSet<Field> = LinkedHashSet(),
                          /**
                           * 请求头说明
                           */
                          var headersExt: LinkedHashSet<Field> = LinkedHashSet(),
                          /**
                           * form参数说明
                           */
                          var parametersExt: LinkedHashSet<Field> = LinkedHashSet(),
                          /**
                           * parts参数说明
                           */
                          var partsExt: LinkedHashSet<Field> = LinkedHashSet(),
                          /**
                           * 请求体参数说明
                           */
                          var contentExt: LinkedHashSet<Field> = LinkedHashSet()
) : OperationRequest(operationRequest.uri, operationRequest.restUri, operationRequest.uriVariables, operationRequest.method, operationRequest.headers, operationRequest.cookies, operationRequest.remoteUser, operationRequest.parameters, operationRequest.parts.apply {
    forEach {
        it.content = if (it.submittedFileName.isNullOrBlank()) it.content else ByteArray(0)
    }
}, operationRequest.content, operationRequest.createdDate) {


}