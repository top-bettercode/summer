package top.bettercode.summer.test

import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
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
                servletRequest.extHeaders[HttpHeaders.CONTENT_TYPE] =
                    arrayOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            }
            filterChain.doFilter(servletRequest, response)
        } else {
            filterChain.doFilter(request, response)
        }
    }
}