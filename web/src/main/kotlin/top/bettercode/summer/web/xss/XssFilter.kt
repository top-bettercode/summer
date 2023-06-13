package top.bettercode.summer.web.xss

import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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