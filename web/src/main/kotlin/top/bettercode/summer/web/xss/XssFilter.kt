package top.bettercode.summer.web.xss

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter

/**
 * XSS过滤
 */
class XssFilter : OncePerRequestFilter(), Ordered {
    override fun getOrder(): Int {
        return Int.MAX_VALUE
    }

    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        val xssRequest = XssHttpServletRequestWrapper(request)
        filterChain.doFilter(xssRequest, response)
    }
}