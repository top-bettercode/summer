package top.bettercode.logging.operation

import top.bettercode.logging.format
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.StringUtils
import java.io.PrintWriter
import java.io.StringWriter

/**
 *
 * @author Peter Wu
 */
object HttpOperation {
    private const val MULTIPART_BOUNDARY = "6o2knFse3p53ty9dmcQvWAIx1zInP11uCfbm"

    fun toString(output: Operation, format: Boolean): String {
        val stringBuilder = StringBuilder("")
        val separatorLine = "------------------------------------------------------------"
        val marginLine = "============================================================"
        stringBuilder.appendLine(marginLine)
        if (output.collectionName.isNotBlank() || output.name.isNotBlank()) {
            stringBuilder.appendLine("${output.collectionName}/${output.name}")
        }
        stringBuilder.appendLine("REQUEST    TIME : ${format(output.request.dateTime)}")
        stringBuilder.appendLine("RESPONSE   TIME : ${format(output.response.dateTime)}")
        stringBuilder.appendLine("DURATION MILLIS : ${output.duration}")
        stringBuilder.appendLine(separatorLine)
        stringBuilder.append(toString(output.request, output.protocol, format))
        stringBuilder.appendLine()
        stringBuilder.append(toString(output.response, output.protocol, format))
        val stackTrace = output.response.stackTrace
        if (stackTrace.isNotBlank()) {
            stringBuilder.appendLine(separatorLine)
            stringBuilder.appendLine("StackTrace:")
            stringBuilder.appendLine(stackTrace)
        }
        stringBuilder.appendLine(marginLine)
        val toString = stringBuilder.toString()
        return Operation.LINE_SEPARATOR + toString
    }

    fun toString(request: OperationRequest, protocol: String, format: Boolean): String {
        val stringBuilder = StringBuilder("")
        stringBuilder.appendLine("${request.method} ${getPath(request)} $protocol")
        getHeaders(request).forEach { k, v -> stringBuilder.appendLine("$k: ${v.joinToString()}") }
        stringBuilder.appendLine(getRequestBody(request, format))
        return stringBuilder.toString()
    }

    fun toString(response: OperationResponse, protocol: String, format: Boolean): String {
        val stringBuilder = StringBuilder("")
        stringBuilder.appendLine("$protocol ${response.statusCode} ${HttpStatus.valueOf(response.statusCode).reasonPhrase}")
        response.headers.forEach { k, v -> stringBuilder.appendLine("$k: ${v.joinToString()}") }
        stringBuilder.appendLine(getResponseBody(response, format))
        return stringBuilder.toString()
    }

    private fun getPath(request: OperationRequest, forceParametersInUri: Boolean = false): String {
        return getPath(request, forceParametersInUri, request.uri.rawPath)
    }

    fun getRestRequestPath(
        request: OperationRequest,
        forceParametersInUri: Boolean = false
    ): String {
        var path = request.restUri
        request.uriVariables.forEach { (t, u) ->
            path = path.replace("{$t}", u)
        }
        return getPath(request, forceParametersInUri, path)
    }

    private fun getPath(
        request: OperationRequest,
        forceParametersInUri: Boolean,
        path: String
    ): String {
        var rpath = path
        var queryString = request.uri.rawQuery
        val uniqueParameters = request.parameters.getUniqueParameters(request.uri)
        if (uniqueParameters.isNotEmpty() && (forceParametersInUri || includeParametersInUri(request))) {
            queryString = if (StringUtils.hasText(queryString)) {
                queryString + "&" + uniqueParameters.toQueryString()
            } else {
                uniqueParameters.toQueryString()
            }
        }
        if (StringUtils.hasText(queryString)) {
            rpath = "$rpath?$queryString"
        }
        return rpath
    }

    private fun includeParametersInUri(request: OperationRequest): Boolean {
        return request.method === HttpMethod.GET || request.method === HttpMethod.DELETE || request.content.isNotEmpty() && !MediaType.APPLICATION_FORM_URLENCODED
            .isCompatibleWith(request.headers.contentType)
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

    private fun getRequestBody(request: OperationRequest, format: Boolean): String {
        val httpRequest = StringWriter()
        val writer = PrintWriter(httpRequest)
        val content = if (format) request.prettyContentAsString else request.contentAsString
        if (StringUtils.hasText(content)) {
            writer.printf("%n%s", content)
        } else if (isPutOrPost(request)) {
            if (request.parts.isEmpty()) {
                val queryString = request.parameters.toQueryString()
                if (StringUtils.hasText(queryString)) {
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
        return HttpMethod.PUT == request.method || HttpMethod.POST == request.method
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
            part.headers.contentType, writer
        )
    }

    private fun writePart(
        name: String, value: String, filename: String?,
        contentType: MediaType?, writer: PrintWriter
    ) {
        writer.printf("Content-Disposition: form-data; name=%s", name)
        if (StringUtils.hasText(filename)) {
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
        return (request.headers[HttpHeaders.CONTENT_TYPE] == null
                && isPutOrPost(request) && !request.parameters.isEmpty() && !includeParametersInUri(
            request
        ))
    }

    private fun getResponseBody(response: OperationResponse, format: Boolean): String {
        val content = if (format) response.prettyContentAsString else response.contentAsString
        return if (content.isEmpty()) content else String.format("%n%s", content)
    }
}