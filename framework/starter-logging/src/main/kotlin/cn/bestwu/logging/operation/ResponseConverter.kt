package cn.bestwu.logging.operation

import cn.bestwu.logging.trace.TraceHttpServletResponseWrapper
import org.springframework.http.HttpHeaders
import org.springframework.util.StringUtils
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

/**
 * A `ResponseConverter` is used to convert an implementation-specific response into
 * an [OperationResponse].
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
        return OperationResponse(
                response.status,
                extractHeaders(response), (response as? TraceHttpServletResponseWrapper)?.contentAsByteArray
                ?: byteArrayOf())
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
        if (StringUtils.hasText(value)) {
            header.append("")
            header.append(value)
        }
    }

    private fun appendIfAvailable(header: StringBuilder, name: String, value: String?) {
        if (StringUtils.hasText(value)) {
            header.append(name)
            header.append(value)
        }
    }

}
