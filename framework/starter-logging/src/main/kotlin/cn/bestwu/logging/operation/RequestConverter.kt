package cn.bestwu.logging.operation

import cn.bestwu.logging.HandlerMethodHandlerInterceptor
import cn.bestwu.logging.RequestLoggingFilter
import cn.bestwu.logging.trace.TraceHttpServletRequestWrapper
import org.springframework.core.convert.ConversionException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.FileCopyUtils
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.servlet.HandlerMapping
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI
import java.util.*
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Part

/**
 * A `RequestConverter` is used to convert an implementation-specific request into
 * an [OperationRequest].
 *
 * @since 2.0.7
</R> */
object RequestConverter {
    private const val SCHEME_HTTP = "http"

    private const val SCHEME_HTTPS = "https"

    private const val STANDARD_PORT_HTTP = 80

    private const val STANDARD_PORT_HTTPS = 443

    /**
     * Converts the given `request` into an `OperationRequest`.
     *
     * @param request the request
     * @return the operation request
     * @throws ConversionException if the conversion fails
     */
    fun convert(request: HttpServletRequest): OperationRequest {
        val createdDate = request.getAttribute(RequestLoggingFilter.REQUEST_CREATED_DATE) as Date
        val headers = extractHeaders(request)
        val parameters = extractParameters(request)
        val parts = extractParts(request)
        val cookies = extractCookies(request, headers)
        val uri = URI.create(getRequestUri(request))
        val restUri = (request.getAttribute(HandlerMethodHandlerInterceptor.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String)
                ?: request.requestURI.substringAfter(request.contextPath)

        @Suppress("UNCHECKED_CAST")
        val uriTemplateVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>
                ?: mapOf()
        val remoteUser = (request.getAttribute(RequestLoggingFilter.REQUEST_LOGGING_USERNAME) as? String)
                ?: request.remoteUser ?: "anonymousUser"

        val content = (request as? TraceHttpServletRequestWrapper)?.contentAsByteArray
                ?: byteArrayOf()
        return OperationRequest(uri, restUri, uriTemplateVariables, HttpMethod.valueOf(request.method), headers, cookies, remoteUser, parameters, parts, content, createdDate)
    }

    private fun extractCookies(request: HttpServletRequest,
                               headers: HttpHeaders): Collection<RequestCookie> {
        if (request.cookies == null || request.cookies!!.isEmpty()) {
            return emptyList()
        }
        val cookies = ArrayList<RequestCookie>()
        for (servletCookie in request.cookies!!) {
            cookies.add(RequestCookie(servletCookie.name, servletCookie.value))
        }
        headers.remove(HttpHeaders.COOKIE)
        return cookies
    }

    @Throws(IOException::class, ServletException::class)
    private fun extractParts(servletRequest: HttpServletRequest): List<OperationRequestPart> {
        val parts = ArrayList<OperationRequestPart>()
        if (servletRequest.contentType?.toLowerCase()?.startsWith("multipart/") == true)
            parts.addAll(extractServletRequestParts(servletRequest))
        if (servletRequest is MultipartHttpServletRequest) {
            parts.addAll(extractMultipartRequestParts(servletRequest))
        } else if (servletRequest is TraceHttpServletRequestWrapper && servletRequest.request is MultipartHttpServletRequest) {
            parts.addAll(extractMultipartRequestParts(servletRequest.request))
        }
        return parts
    }

    @Throws(IOException::class, ServletException::class)
    private fun extractServletRequestParts(
            servletRequest: HttpServletRequest): List<OperationRequestPart> {
        val parts = ArrayList<OperationRequestPart>()
        for (part in servletRequest.parts) {
            parts.add(createOperationRequestPart(part))
        }
        return parts
    }

    @Throws(IOException::class)
    private fun createOperationRequestPart(part: Part): OperationRequestPart {
        val partHeaders = extractHeaders(part)
        val contentTypeHeader = partHeaders[HttpHeaders.CONTENT_TYPE]
        if (part.contentType != null && contentTypeHeader == null) {
            partHeaders.contentType = MediaType.parseMediaType(part.contentType)
        }

        val content = try {
            FileCopyUtils.copyToByteArray(part.inputStream)
        } catch (e: Exception) {
            "Request part has been read.Can't record the original data.".toByteArray()
        }
        return OperationRequestPart(part.name,
                if (StringUtils.hasText(part.submittedFileName))
                    part.submittedFileName
                else
                    null, partHeaders,
                content)
    }

    @Throws(IOException::class)
    private fun extractMultipartRequestParts(multipartRequest: MultipartHttpServletRequest): List<OperationRequestPart> {
        val parts = ArrayList<OperationRequestPart>()
        for (entry in multipartRequest.multiFileMap.entries) {
            for (file in entry.value) {
                parts.add(createOperationRequestPart(file))
            }
        }
        return parts
    }

    @Throws(IOException::class)
    private fun createOperationRequestPart(file: MultipartFile): OperationRequestPart {
        val partHeaders = HttpHeaders()
        if (StringUtils.hasText(file.contentType)) {
            partHeaders.contentType = MediaType.parseMediaType(file.contentType!!)
        }
        val content = try {
            file.bytes
        } catch (e: Exception) {
            "Request part has been read.Can't record the original data.".toByteArray()
        }
        return OperationRequestPart(file.name,
                if (StringUtils.hasText(file.originalFilename))
                    file.originalFilename
                else
                    null,
                partHeaders,
                content)
    }

    private fun extractHeaders(part: Part): HttpHeaders {
        val partHeaders = HttpHeaders()
        for (headerName in part.headerNames) {
            for (value in part.getHeaders(headerName)) {
                partHeaders.add(headerName, value)
            }
        }
        return partHeaders
    }

    private fun extractParameters(servletRequest: HttpServletRequest): Parameters {
        val parameters = Parameters()
        for (name in servletRequest.parameterNames) {
            for (value in servletRequest.getParameterValues(name)) {
                parameters.add(name, value)
            }
        }
        return parameters
    }

    private fun extractHeaders(servletRequest: HttpServletRequest): HttpHeaders {
        val headers = HttpHeaders()
        for (headerName in servletRequest.headerNames) {
            for (value in servletRequest.getHeaders(headerName)) {
                headers.add(headerName, value)
            }
        }
        return headers
    }

    private fun isNonStandardPort(request: HttpServletRequest): Boolean {
        return (((SCHEME_HTTP == request.scheme && request.serverPort != STANDARD_PORT_HTTP)) || ((SCHEME_HTTPS == request.scheme && request.serverPort != STANDARD_PORT_HTTPS)))
    }

    private fun getRequestUri(request: HttpServletRequest): String {
        val uriWriter = StringWriter()
        val printer = PrintWriter(uriWriter)

        printer.printf("%s://%s", request.scheme, request.serverName)
        if (isNonStandardPort(request)) {
            printer.printf(":%d", request.serverPort)
        }

        printer.print(request.requestURI)
        return uriWriter.toString()
    }

}
