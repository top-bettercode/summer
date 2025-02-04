package top.bettercode.summer.tools.lang.operation

import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.random.Random

/**
 *
 * @author Peter Wu
 */
object HttpOperation {
    private const val MULTIPART_BOUNDARY = "6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm"

    const val MDC_TRACEID = "traceid"
    const val REQUEST_LOG_MARKER = "request"
    val REQUEST_DATE_TIME = HttpOperation::class.java.name + ".dateTime"
    val BEST_MATCHING_PATTERN_ATTRIBUTE = HttpOperation::class.java.name + ".bestMatchingPattern"

    @JvmField
    val REQUEST_LOGGING_USERNAME = HttpOperation::class.java.name + ".username"
    const val SEPARATOR_LINE = "------------------------------------------------------------"


    @JvmStatic
    fun traceid(): String {
        return MDC.get(MDC_TRACEID) ?: Integer.toHexString(Random.nextInt())
    }

    @JvmStatic
    fun appendTraceid(): String {
        val traceId = MDC.get(MDC_TRACEID)
        return if (traceId == null) {
            Integer.toHexString(Random.nextInt())
        } else if (traceId.contains("-")) {
            val traceIdArray = traceId.split("-")
            val mainId = traceIdArray[0]
            val subId = traceIdArray[1].toInt()
            "$mainId-${subId + 1}"
        } else {
            "$traceId-1"
        }
    }

    fun toString(
        output: Operation,
        format: Boolean,
        requestDecrypt: ((ByteArray) -> ByteArray)? = null,
        responseDecrypt: ((ByteArray) -> ByteArray)? = null
    ): String {
        val stringBuilder = StringBuilder("")
        val marginLine = "============================================================"
        stringBuilder.appendLine(marginLine)
        if (output.collectionName.isNotBlank() || output.name.isNotBlank()) {
            stringBuilder.appendLine("${output.collectionName}/${output.name}")
        }
        stringBuilder.appendLine("USERNAME        : ${output.request.remoteUser}")
        stringBuilder.appendLine(
            "REQUEST    TIME : ${
                TimeUtil.format(
                    output.request.dateTime,
                    DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
                )
            }"
        )
        stringBuilder.appendLine(
            "RESPONSE   TIME : ${
                TimeUtil.format(
                    output.response.dateTime,
                    DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
                )
            }"
        )
        stringBuilder.appendLine("DURATION MILLIS : ${output.duration}")
        stringBuilder.appendLine(SEPARATOR_LINE)
        stringBuilder.append(toString(output.request, output.protocol, format, requestDecrypt))
        stringBuilder.appendLine()
        stringBuilder.append(toString(output.response, output.protocol, format, responseDecrypt))
        val stackTrace = output.response.stackTrace
        if (stackTrace.isNotBlank()) {
            stringBuilder.appendLine(SEPARATOR_LINE)
            stringBuilder.appendLine("StackTrace:")
            stringBuilder.appendLine(stackTrace)
        }
        stringBuilder.appendLine(marginLine)
        val toString = stringBuilder.toString()
        return Operation.LINE_SEPARATOR + toString
    }

    fun toString(
        request: OperationRequest,
        protocol: String = RequestConverter.DEFAULT_PROTOCOL,
        format: Boolean = true,
        decrypt: ((ByteArray) -> ByteArray)? = null
    ): String {
        val stringBuilder = StringBuilder("")
        stringBuilder.appendLine("${request.method} ${getRequestPath(request)} $protocol")
        getHeaders(request).forEach { k, v -> stringBuilder.appendLine("$k: ${v.joinToString()}") }
        if (decrypt != null) {
            request.content = decrypt(request.content)
            stringBuilder.appendLine()
            stringBuilder.appendLine("---- decrypted content ----")
        }
        val body = getRequestBody(request, format)
        if (body.isNotBlank())
            stringBuilder.appendLine(body)
        if (decrypt != null) {
            stringBuilder.appendLine()
            stringBuilder.appendLine("---- decrypted content ----")
        }
        return stringBuilder.toString()
    }

    fun toString(
        response: OperationResponse,
        protocol: String,
        format: Boolean,
        decrypt: ((ByteArray) -> ByteArray)? = null
    ): String {
        val stringBuilder = StringBuilder("")
        stringBuilder.appendLine(
            "$protocol ${response.statusCode} ${
                try {
                    HttpStatus.valueOf(response.statusCode).reasonPhrase
                } catch (e: Exception) {
                    ""
                }
            }"
        )
        response.headers.forEach { k, v -> stringBuilder.appendLine("$k: ${v.joinToString()}") }
        val decryptedContent = "---- decrypted content ----"
        if (decrypt != null) {
            response.content = decrypt(response.content)
            stringBuilder.appendLine()
            stringBuilder.appendLine(decryptedContent)
        }
        stringBuilder.appendLine(getResponseBody(response, format))
        if (decrypt != null) {
            stringBuilder.appendLine()
            stringBuilder.appendLine(decryptedContent)
        }
        return stringBuilder.toString()
    }

    fun getRequestPath(
        request: OperationRequest
    ): String {
        val uri = request.uri
        val path = uri.rawPath
        val rawQuery = uri.rawQuery
        return "$path${if (rawQuery.isNullOrBlank()) "" else "?${rawQuery}"}"
    }

    private fun getHeaders(request: OperationRequest): HttpHeaders {
        val headers = HttpHeaders()

        for (header in request.headers.entries) {
            for (value in header.value) {
                if (HttpHeaders.CONTENT_TYPE == header.key && !request.parts.isEmpty()) {
                    headers.add(
                        header.key,
                        String.format("%s; boundary=%s", value, MULTIPART_BOUNDARY)
                    )
                } else {
                    headers.add(header.key, value)
                }

            }
        }

        for (cookie in request.cookies) {
            headers.add(
                HttpHeaders.COOKIE,
                String.format("%s=%s", cookie.name, cookie.value)
            )
        }

        if (requiresFormEncodingContentTypeHeader(request)) {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        }
        return headers
    }

    private fun getRequestBody(
        request: OperationRequest,
        format: Boolean
    ): String {
        val httpRequest = StringWriter()
        val writer = PrintWriter(httpRequest)
        val content = if (format) request.prettyContentAsString else request.contentAsString
        if (content.isNotBlank()) {
            writer.printf("%n%s", content)
        } else {
            if (request.parts.isEmpty()) {
                val queryString = request.parameters.toQueryString()
                if (queryString.isNotBlank()) {
                    writer.println()
                    writer.print(queryString)
                }
            } else {
                writeParts(request, writer)
            }
        }
        return httpRequest.toString()
    }


    fun isPutOrPost(request: OperationRequest): Boolean {
        return HttpMethod.PUT.name == request.method || HttpMethod.POST.name == request.method
    }

    private fun writeParts(request: OperationRequest, writer: PrintWriter) {
        writer.println()
        for (parameter in request.parameters.entries) {
            if (parameter.value.isEmpty()) {
                writePartBoundary(writer)
                writePart(parameter.key, "", null, null, writer)
            } else {
                for (value in parameter.value) {
                    writePartBoundary(writer)
                    writePart(parameter.key, value, null, null, writer)
                    writer.println()
                }
            }
        }
        for (part in request.parts) {
            writePartBoundary(writer)
            writePart(part, writer)
            writer.println()
        }
        writeMultipartEnd(writer)
    }

    private fun writePartBoundary(writer: PrintWriter) {
        writer.printf("--%s%n", MULTIPART_BOUNDARY)
    }

    private fun writePart(part: OperationRequestPart, writer: PrintWriter) {
        writePart(
            part.name, part.contentAsString, part.submittedFileName,
            part.contentType, writer
        )
    }

    private fun writePart(
        name: String, value: String, filename: String?,
        contentType: MediaType?, writer: PrintWriter
    ) {
        writer.printf("Content-Disposition: form-data; name=%s", name)
        if (!filename.isNullOrBlank()) {
            writer.printf("; filename=%s", filename)
        }
        writer.printf("%n")
        if (contentType != null) {
            writer.printf("Content-Type: %s%n", contentType)
        }
        writer.println()
        writer.print(value)
    }

    private fun writeMultipartEnd(writer: PrintWriter) {
        writer.printf("--%s--", MULTIPART_BOUNDARY)
    }

    private fun requiresFormEncodingContentTypeHeader(request: OperationRequest): Boolean {
        return request.headers[HttpHeaders.CONTENT_TYPE] == null && isPutOrPost(request) && !request.parameters.isEmpty()
    }

    private fun getResponseBody(
        response: OperationResponse,
        format: Boolean
    ): String {
        val content = if (format) response.prettyContentAsString else response.contentAsString
        return if (content.isEmpty()) content else String.format("%n%s", content)
    }
}