package cn.bestwu.autodoc.core.operation

import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.logging.operation.OperationResponse
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder("contentExt", "headersExt", "statusCode", "contentAsString", "stackTrace")
@JsonIgnoreProperties(value = ["headers", "dateTime"],allowSetters = true)
class DocOperationResponse(operationResponse: OperationResponse = OperationResponse(),
                           /**
                            * 请求头说明
                            */
                           var headersExt: LinkedHashSet<Field> = LinkedHashSet(),
                           /**
                            * 响应体说明
                            */
                           var contentExt: LinkedHashSet<Field> = LinkedHashSet()
) : OperationResponse(statusCode = operationResponse.statusCode, headers = operationResponse.headers, content = operationResponse.content, dateTime = operationResponse.dateTime, stackTrace = operationResponse.stackTrace)