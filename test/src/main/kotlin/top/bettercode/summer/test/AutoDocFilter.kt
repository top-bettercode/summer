package top.bettercode.summer.test

import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.filter.OncePerRequestFilter
import top.bettercode.summer.tools.lang.util.StringUtil
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Peter Wu
 */
class AutoDocFilter(
    private val handlers: List<AutoDocRequestHandler>?
) : OncePerRequestFilter(), Ordered {

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!handlers.isNullOrEmpty()) {
            val servletRequest = AutoDocHttpServletRequest(request)
            handlers.filter { it.support(servletRequest) }.forEach { it.handle(servletRequest) }
            if (servletRequest.contentType.isNullOrBlank() && (HttpMethod.PUT.name == request.method || HttpMethod.POST.name == request.method)) {
                if (request is MockHttpServletRequest && request.contentLengthLong > 0L && isJson(
                        request.contentAsByteArray!!
                    )
                ) {
                    servletRequest.header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                    )
                } else {
                    servletRequest.header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    )
                }
            }
            //user-agent 未设置时，设置默认：Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/117.0
            if (servletRequest.getHeaders(HttpHeaders.USER_AGENT)?.hasMoreElements() != true) {
                servletRequest.header(
                    HttpHeaders.USER_AGENT,
                    "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/117.0"
                )
            }
            filterChain.doFilter(servletRequest, response)
        } else {
            filterChain.doFilter(request, response)
        }
    }

    fun isJson(content: ByteArray): Boolean {
        return try {
            StringUtil.objectMapper().readTree(content)
            true
        } catch (e: Exception) {
            false
        }
    }
}