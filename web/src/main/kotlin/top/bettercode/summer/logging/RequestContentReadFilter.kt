package top.bettercode.summer.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter
import top.bettercode.summer.tools.lang.operation.RequestConverter
import top.bettercode.summer.tools.lang.trace.TraceHttpServletRequestWrapper

/**
 * @author Peter Wu
 */
class RequestContentReadFilter : OncePerRequestFilter(), Ordered {
    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 2
    }

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } finally {
            val traceHttpServletRequestWrapper = RequestConverter.getRequestWrapper(
                    request, TraceHttpServletRequestWrapper::class.java
            )
            traceHttpServletRequestWrapper?.read()
        }
    }
}