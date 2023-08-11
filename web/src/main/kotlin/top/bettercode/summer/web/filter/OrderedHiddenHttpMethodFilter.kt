package top.bettercode.summer.web.filter

import org.springframework.core.Ordered
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import org.springframework.web.filter.HiddenHttpMethodFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

/**
 * 对delete,put隐藏方法的处理
 *
 * @author Peter Wu
 */
class OrderedHiddenHttpMethodFilter : HiddenHttpMethodFilter(), Ordered {
    private var order = DEFAULT_ORDER
    private var methodParam = DEFAULT_METHOD_PARAM

    /**
     * Set the parameter name to look for HTTP methods.
     *
     * @param methodParam methodParam
     * @see .DEFAULT_METHOD_PARAM
     */
    override fun setMethodParam(methodParam: String) {
        Assert.hasText(methodParam, "'methodParam' must not be empty")
        this.methodParam = methodParam
    }

    override fun getOrder(): Int {
        return order
    }

    /**
     * Set the order for this filter.
     *
     * @param order the order to set
     */
    fun setOrder(order: Int) {
        this.order = order
    }

    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse, filterChain: FilterChain) {
        val paramValue = request.getParameter(this.methodParam)
        if (StringUtils.hasLength(paramValue)) {
            val method1 = request.method
            val method = paramValue.uppercase()
            if ("POST" == method1 && "PUT" == method || "GET" == method1 && "DELETE" == method) {
                val wrapper: HttpServletRequest = HttpMethodRequestWrapper(
                        request, method)
                filterChain.doFilter(wrapper, response)
            }
        } else {
            filterChain.doFilter(request, response)
        }
    }

    /**
     * Simple [HttpServletRequest] wrapper that returns the supplied method for
     * [HttpServletRequest.getMethod].
     */
    private class HttpMethodRequestWrapper(request: HttpServletRequest?, private val method: String) : HttpServletRequestWrapper(request) {
        override fun getMethod(): String {
            return method
        }
    }

    companion object {
        /**
         * The default order is high to ensure the filter is applied before Spring Security.
         */
        const val DEFAULT_ORDER = -10000

        /**
         * Default method parameter: `_method`
         */
        const val DEFAULT_METHOD_PARAM = "_method"
    }
}
