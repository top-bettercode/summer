package cn.bestwu.logging.operation

import cn.bestwu.logging.LogFormat
import cn.bestwu.logging.RequestLoggingConfig
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.http.HttpHeaders
import java.text.SimpleDateFormat
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
         * @return a <code>String</code> containing the protocol name and version
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
        open var response: OperationResponse = OperationResponse()) {

    fun toString(config: RequestLoggingConfig): String {
        val originHeaders = request.headers
        val originParameters = request.parameters
        val originRequestContent = request.content
        val originStackTrace = response.stackTrace
        val originResponseContent = response.content

        val headers = if (config.encryptHeaders.isEmpty()) {
            originHeaders
        } else {
            val headers = HttpHeaders()
            originHeaders.forEach { k, v ->
                if (config.encryptHeaders.contains(k)) {
                    headers.set(k, encryptedString)
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
                    parameters.set(k, encryptedString)
                } else {
                    parameters[k] = v
                }
            }
            parameters
        }
        val format = config.format
        request.headers = headers
        request.parameters = parameters
        request.content = if (config.includeRequestBody || originRequestContent.isEmpty()) {
            (if (format) request.prettyContent else originRequestContent)
        } else "unrecorded".toByteArray()


        response.content = if (config.includeResponseBody || originResponseContent.isEmpty()) {
            (if (format) response.prettyContent else originResponseContent)
        } else "unrecorded".toByteArray()

        response.stackTrace = if (config.includeTrace || originStackTrace.isBlank()) originStackTrace else "unrecorded"

        val log = when (config.logFormat) {
            LogFormat.HTTP -> {
                HttpOperation.toString(this, format)
            }
            LogFormat.JSON -> {
                (if (format) LINE_SEPARATOR else "") + valueOf(this, format)
            }
        }
        request.headers = originHeaders
        request.parameters = originParameters
        request.content = originRequestContent
        response.content = originResponseContent
        response.stackTrace = originStackTrace
        return log
    }


    /**
     * 请求耗时，单位毫秒
     */
    val duration: Long
        get() = response.dateTime.until(request.dateTime, ChronoUnit.MILLIS)

    companion object {
        const val encryptedString = "******"

        @JvmField
        val LINE_SEPARATOR = System.getProperty("line.separator")!!

        @JvmStatic
        var OBJECT_MAPPER = ObjectMapper()

        @JvmStatic
        var INDENT_OUTPUT_OBJECT_MAPPER = ObjectMapper()

        init {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")
            OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            OBJECT_MAPPER.dateFormat = simpleDateFormat
            INDENT_OUTPUT_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            INDENT_OUTPUT_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT)
            INDENT_OUTPUT_OBJECT_MAPPER.dateFormat = simpleDateFormat
        }

        /**
         * 转换为字符串
         *
         * @param object 对象
         * @param format 是否格式化输出
         * @return 字符串
         */
        @JvmOverloads
        @JvmStatic
        fun valueOf(`object`: Any?, format: Boolean = false): String {
            if (`object` is CharSequence) {
                return `object`.toString()
            }
            return if (format) {
                INDENT_OUTPUT_OBJECT_MAPPER.writeValueAsString(`object`)
            } else {
                OBJECT_MAPPER.writeValueAsString(`object`)
            }
        }
    }
}
