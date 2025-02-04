package top.bettercode.summer.tools.autodoc.operation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.lang.operation.OperationResponse

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder("contentExt", "headersExt", "statusCode", "contentAsString")
@JsonIgnoreProperties(value = ["headers", "dateTime", "stackTrace"], allowSetters = true)
class DocOperationResponse(
        operationResponse: OperationResponse = OperationResponse(),
        /**
         * 请求头说明
         */
        var headersExt: LinkedHashSet<Field> = LinkedHashSet(),
        /**
         * 响应体说明
         */
        var contentExt: LinkedHashSet<Field> = LinkedHashSet()
) : OperationResponse(
        statusCode = operationResponse.statusCode,
        headers = operationResponse.headers,
        content = operationResponse.content,
        dateTime = operationResponse.dateTime,
        stackTrace = operationResponse.stackTrace
)