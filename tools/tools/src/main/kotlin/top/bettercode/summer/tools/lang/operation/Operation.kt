package top.bettercode.summer.tools.lang.operation

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.http.HttpHeaders
import java.time.temporal.ChronoUnit

/**
 * Describes an operation performed on a RESTful service.
 */
@JsonPropertyOrder("collectionName", "name", "protocol", "duration", "request", "response")
open class Operation(
        /**
         *
         * 操作集合名称
         *
         * @retrun the collectionName
         */
        var collectionName: String = "",
        /**
         * Returns the name of the operation.
         *
         * the name
         */
        var name: String = "",
        /**
         * Returns the name and version of the protocol the request uses in the form
         * <i>protocol/majorVersion.minorVersion</i>, for example, HTTP/1.1. For
         * HTTP servlets, the value returned is the same as the value of the CGI
         * variable <code>SERVER_PROTOCOL</code>.
         *
         * a <code>String</code> containing the protocol name and version
         *         number
         */
        var protocol: String = "",

        /**
         * Returns the request that was sent.
         *
         * the request
         */
        open var request: OperationRequest = OperationRequest(),

        /**
         * Returns the response that was received.
         *
         * the response
         */
        open var response: OperationResponse = OperationResponse()
) {


    @JvmOverloads
    fun toString(
            config: RequestLoggingConfig,
            requestDecrypt: ((ByteArray) -> ByteArray)? = null,
            responseDecrypt: ((ByteArray) -> ByteArray)? = null
    ): String {
        val originHeaders = request.headers
        val originParameters = request.parameters
        val originParts = request.parts
        val originRequestContent = request.content
        val originResponseContent = response.content
        val originStackTrace = response.stackTrace

        val headers = if (config.encryptHeaders.isEmpty()) {
            originHeaders
        } else {
            val headers = HttpHeaders()
            originHeaders.forEach { k, v ->
                if (config.encryptHeaders.contains(k)) {
                    headers.set(k, ENCRYPTED_STRING)
                } else {
                    headers[k] = v
                }
            }
            headers
        }

        val parameters = if (config.encryptParameters.isEmpty()) {
            originParameters
        } else {
            val parameters = Parameters()
            originParameters.forEach { k, v ->
                if (config.encryptParameters.contains(k)) {
                    parameters.set(k, ENCRYPTED_STRING)
                } else {
                    parameters[k] = v
                }
            }
            parameters
        }
        request.headers = headers
        request.parameters = parameters

        val error = response.statusCode >= 400

        request.parts = originParts.map {
            if (config.includeRequestBody || error || it.submittedFileName.isNullOrBlank() || it.content.isEmpty()) {
                it
            } else OperationRequestPart(
                    name = it.name,
                    submittedFileName = it.submittedFileName,
                    headers = it.headers,
                    content = UNRECORDED_MARK.toByteArray()
            )
        }

        request.content =
                if (config.includeRequestBody || error || originRequestContent.isEmpty()) {
                    originRequestContent
                } else UNRECORDED_MARK.toByteArray()


        response.content =
                if (config.includeResponseBody || error || originResponseContent.isEmpty()) {
                    originResponseContent
                } else UNRECORDED_MARK.toByteArray()

        response.stackTrace =
                if (config.includeTrace || originStackTrace.isBlank()) originStackTrace else UNRECORDED_MARK

        val log = HttpOperation.toString(this, config.format, requestDecrypt, responseDecrypt)

        request.headers = originHeaders
        request.parameters = originParameters
        request.parts = originParts
        request.content = originRequestContent
        response.content = originResponseContent
        response.stackTrace = originStackTrace
        return log
    }


    /**
     * 请求耗时，单位毫秒
     */
    val duration: Long
        get() = request.dateTime.until(response.dateTime, ChronoUnit.MILLIS)

    companion object {
        const val ENCRYPTED_STRING = "******"
        const val UNRECORDED_MARK = "[UNRECORDED]"

        @JvmField
        val LINE_SEPARATOR: String = System.lineSeparator()

    }
}
