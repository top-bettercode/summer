package top.bettercode.logging.operation

import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.core.convert.ConversionException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.Assert
import org.springframework.util.FileCopyUtils
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.servlet.HandlerMapping
import top.bettercode.lang.util.IPAddressUtil
import top.bettercode.logging.RequestLoggingFilter
import top.bettercode.logging.client.ClientHttpRequestWrapper
import top.bettercode.logging.trace.TraceHttpServletRequestWrapper
import top.bettercode.logging.trace.TracePart
import top.bettercode.simpleframework.support.ApplicationContextHolder
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI
import java.time.LocalDateTime
import java.util.*
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.Part

/**
 * A `RequestConverter` is used to convert an implementation-specific request into
 * an OperationRequest.
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
        val dateTime = request.getAttribute(RequestLoggingFilter.REQUEST_DATE_TIME) as LocalDateTime
        val headers = extractHeaders(request)
        val parameters = extractParameters(request)
        val parts = extractParts(request)
        val cookies = extractCookies(request, headers)
        val uri = URI.create(getRequestUri(request))
        val restUri =
            (request.getAttribute(RequestLoggingFilter.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String)
                ?: request.servletPath

        @Suppress("UNCHECKED_CAST")
        val uriTemplateVariables =
            request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>
                ?: mapOf()
        val remoteUser =
            (request.getAttribute(RequestLoggingFilter.REQUEST_LOGGING_USERNAME) as? String)
                ?: request.remoteUser ?: "anonymous"

        val content = (request as? TraceHttpServletRequestWrapper)?.contentAsByteArray
            ?: try {
                FileCopyUtils.copyToByteArray(request.inputStream)
            } catch (e: Exception) {
                "Request inputStream has been read.Can't record the original data.".toByteArray()
            }
        return OperationRequest(
            uri = uri,
            restUri = restUri,
            uriVariables = uriTemplateVariables,
            method = HttpMethod.valueOf(request.method),
            headers = headers,
            cookies = cookies,
            remoteUser = remoteUser,
            parameters = parameters,
            parts = parts,
            content = content,
            dateTime = dateTime
        )
    }

    fun convert(request: ClientHttpRequestWrapper, dateTime: LocalDateTime): OperationRequest {
        val headers = HttpHeaders()
        val cookies = request.headers[HttpHeaders.COOKIE]?.map {
            RequestCookie(it.substringBefore("="), it.substringAfter("="))
        } ?: listOf()

        val uri = request.uri

        headers.add(HttpHeaders.HOST, uri.authority)
        headers.addAll(request.headers)
        headers.remove(HttpHeaders.COOKIE)

        val restUri = uri.toString()
        val content = request.record.toByteArray()
        return OperationRequest(
            uri = uri,
            restUri = restUri,
            uriVariables = mapOf(),
            method = request.method,
            headers = headers,
            cookies = cookies,
            remoteUser = "NonSpecificUser",
            parameters = Parameters().getUniqueParameters(uri),
            parts = listOf(),
            content = content,
            dateTime = dateTime
        )
    }

    private fun extractCookies(
        request: HttpServletRequest,
        headers: HttpHeaders
    ): Collection<RequestCookie> {
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
        val request: ServletRequest = unwrap(servletRequest)

        val parts = ArrayList<OperationRequestPart>()
        if (request is HttpServletRequest && request.contentType?.lowercase(Locale.getDefault())
                ?.startsWith("multipart/") == true
        )
            parts.addAll(extractServletRequestParts(request))
        if (request is MultipartHttpServletRequest) {
            parts.addAll(extractMultipartRequestParts(request))
        }
        return parts
    }

    private fun unwrap(servletRequest: ServletRequest): ServletRequest {
        return if (servletRequest is HttpServletRequestWrapper) {
            unwrap(servletRequest.request)
        } else
            servletRequest
    }

    @Throws(IOException::class, ServletException::class)
    private fun extractServletRequestParts(
        servletRequest: HttpServletRequest
    ): List<OperationRequestPart> {
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

        val content = (part as? TracePart)?.contentAsByteArray ?: try {
            FileCopyUtils.copyToByteArray(part.inputStream)
        } catch (e: Exception) {
            "Request part has been read.Can't record the original data.".toByteArray()
        }
        return OperationRequestPart(
            part.name,
            if (StringUtils.hasText(part.submittedFileName))
                part.submittedFileName
            else
                null, partHeaders,
            content
        )
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
        return OperationRequestPart(
            file.name,
            if (StringUtils.hasText(file.originalFilename))
                file.originalFilename
            else
                null,
            partHeaders,
            content
        )
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

    private fun isNonStandardPort(
        request: HttpServletRequest,
        serverPort: Int = request.serverPort
    ): Boolean {
        return (((SCHEME_HTTP == request.scheme && serverPort != STANDARD_PORT_HTTP)) || ((SCHEME_HTTPS == request.scheme && serverPort != STANDARD_PORT_HTTPS)))
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

    fun getRequestPath(request: HttpServletRequest): String {
        val uriWriter = StringWriter()
        val printer = PrintWriter(uriWriter)

        printer.printf("%s://%s", request.scheme, request.serverName)
        if (isNonStandardPort(request)) {
            printer.printf(":%d", request.serverPort)
        }
        if ("/" != request.contextPath)
            printer.print(request.contextPath)
        return uriWriter.toString()
    }

    val apiHost: String? by lazy {
        val uriWriter = StringWriter()
        val printer = PrintWriter(uriWriter)
        val serverProperties = ApplicationContextHolder.getBean(ServerProperties::class.java)
        Assert.notNull(serverProperties, "serverProperties must not be null")
        val serverPort = serverProperties.port ?: 8080
        printer.printf("%s://%s", SCHEME_HTTP, IPAddressUtil.inet4Address)
        if (serverPort != STANDARD_PORT_HTTP) {
            printer.printf(":%d", serverPort)
        }
        val contextPath = serverProperties.servlet?.contextPath ?: "/"
        if ("/" != contextPath)
            printer.print(contextPath)
        uriWriter.toString()
    }
}
