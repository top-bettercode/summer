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
@JsonPropertyOrder("contentExt", "headersExt", "status", "headers", "contentAsString", "stackTrace")
@JsonIgnoreProperties("createdDate")
class DocOperationResponse(operationResponse: OperationResponse = OperationResponse(),
                           /**
                            * 请求头说明
                            */
                           var headersExt: SortedSet<Field> = TreeSet(),
                           /**
                            * 响应体说明
                            */
                           var contentExt: SortedSet<Field> = TreeSet()
) : OperationResponse(statusCode = operationResponse.statusCode, headers = operationResponse.headers, content = operationResponse.content, createdDate = operationResponse.createdDate, stackTrace = operationResponse.stackTrace)