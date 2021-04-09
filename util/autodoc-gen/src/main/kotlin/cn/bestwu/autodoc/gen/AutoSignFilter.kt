package cn.bestwu.autodoc.gen

import cn.bestwu.api.sign.ApiSignAlgorithm
import cn.bestwu.api.sign.ApiSignProperties
import cn.bestwu.autodoc.gen.Autodoc.requiredHeaders
import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

/**
 * @author Peter Wu
 */
class AutoSignFilter(private val apiSignProperties: ApiSignProperties,
                     private val apiSignAlgorithm: ApiSignAlgorithm) : OncePerRequestFilter(), Ordered {
    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse,
                                  filterChain: FilterChain) {
        if (requiredHeaders.contains(apiSignProperties.parameterName)) {
            filterChain.doFilter(SignHttpServletRequestWrapper(request), response)
        } else {
            filterChain.doFilter(request, response)
        }
    }

    internal inner class SignHttpServletRequestWrapper(request: HttpServletRequest?) : HttpServletRequestWrapper(request) {
        private val sign: String
        override fun getHeaderNames(): Enumeration<String> {
            val names: MutableList<String> = ArrayList()
            val headerNames = super.getHeaderNames()
            while (headerNames.hasMoreElements()) {
                names.add(headerNames.nextElement())
            }
            names.add(apiSignProperties.parameterName)
            return Enumerator(names)
        }

        override fun getHeader(name: String): String? {
            return if (apiSignProperties.parameterName == name) {
                sign
            } else {
                super.getHeader(name)
            }
        }

        override fun getHeaders(name: String): Enumeration<String> {
            return if (apiSignProperties.parameterName == name) {
                Enumerator(setOf(sign))
            } else {
                super.getHeaders(name)
            }
        }

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        init {
            sign = apiSignAlgorithm.sign(request!!)
        }
    }
}