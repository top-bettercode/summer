package top.bettercode.summer.tools.lang.operation

import org.springframework.http.HttpHeaders
import top.bettercode.summer.tools.lang.client.ClientHttpResponseWrapper
import top.bettercode.summer.tools.lang.trace.TraceHttpServletResponseWrapper
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

/**
 * A `ResponseConverter` is used to convert an implementation-specific response into
 * an OperationResponse.
 *
 * @param <R> The implementation-specific response type
 * @since 2.0.7
</R> */
object ResponseConverter {

    /**
     * Converts the given `response` into an `OperationResponse`.
     *
     * @param response the response
     * @return the operation response
     */
    fun convert(response: HttpServletResponse): OperationResponse {
        val responseWrapper =
                getResponseWrapper(response, TraceHttpServletResponseWrapper::class.java)
        return OperationResponse(
                response.status,
                extractHeaders(responseWrapper ?: response), responseWrapper?.contentAsByteArray
                ?: byteArrayOf()
        )
    }


    fun convert(response: ClientHttpResponseWrapper): OperationResponse {
        return OperationResponse(
                response.statusCode.value(),
                response.headers, response.content
        )
    }

    fun unwrapHttpServletResponse(response: ServletResponse): ServletResponse {
        return when (response) {
            is HttpServletResponseWrapper -> {
                unwrapHttpServletResponse(response.response)
            }

            else -> {
                response
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : HttpServletResponseWrapper> getResponseWrapper(
            response: ServletResponse, responseType: Class<T>
    ): T? {
        return if (responseType.isInstance(response)) {
            response as T
        } else if (response is HttpServletResponseWrapper) {
            getResponseWrapper(response.response, responseType)
        } else {
            null
        }
    }

    private fun extractHeaders(response: HttpServletResponse): HttpHeaders {
        val headers = HttpHeaders()
        for (headerName in response.headerNames) {
            for (value in response.getHeaders(headerName)) {
                headers.add(headerName, value)
            }
        }
        if (response is TraceHttpServletResponseWrapper)
            if (response.cookies.isNotEmpty() && !headers.containsKey(HttpHeaders.SET_COOKIE)) {
                for (cookie in response.cookies) {
                    headers.add(HttpHeaders.SET_COOKIE, generateSetCookieHeader(cookie))
                }
            }

        return headers
    }

    private fun generateSetCookieHeader(cookie: Cookie): String {
        val header = StringBuilder()

        header.append(cookie.name)
        header.append('=')

        appendIfAvailable(header, cookie.value)

        val maxAge = cookie.maxAge
        if (maxAge > -1) {
            header.append(";Max-Age=")
            header.append(maxAge)
        }

        appendIfAvailable(header, "; Domain=", cookie.domain)
        appendIfAvailable(header, "; Path=", cookie.path)

        if (cookie.secure) {
            header.append("; Secure")
        }

        if (cookie.isHttpOnly) {
            header.append("; HttpOnly")
        }

        return header.toString()
    }

    private fun appendIfAvailable(header: StringBuilder, value: String?) {
        if (!value.isNullOrBlank()) {
            header.append("")
            header.append(value)
        }
    }

    private fun appendIfAvailable(header: StringBuilder, name: String, value: String?) {
        if (!value.isNullOrBlank()) {
            header.append(name)
            header.append(value)
        }
    }

}
