package cn.bestwu.logging.operation

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.util.*

/**
 * The response that was received as part of performing an operation on a RESTful service.
 */
@JsonPropertyOrder("status", "headers", "contentAsString", "createdDate", "stackTrace")
open class OperationResponse(

        /**
         * Returns the status of the response.
         *
         * the statusCode
         */
        var statusCode: Int = HttpStatus.OK.value(),

        /**
         * Returns the headers in the response.
         *
         * the headers
         */
        headers: HttpHeaders = HttpHeaders(),
        /**
         * Returns the content of the response. If the response has no content an empty array
         * is returned.
         *
         * the contents, never `null`
         */
        content: ByteArray = ByteArray(0),
        /**
         * 响应时间
         */
        var createdDate: Date = Date(),
        /**
         * 异常追踪桟
         */
        var stackTrace: String = ""
) : AbstractOperationMessage(headers, content)