package top.bettercode.summer.web.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 支持PUT DELETE form提交
 *
 * @author Peter Wu
 */
class ApiVersionFilter(private val apiVersionService: IApiVersionService) : OncePerRequestFilter(), Ordered {
    private var order = DEFAULT_ORDER
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
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        response.setHeader(apiVersionService.versionName, apiVersionService.version)
        response.setHeader(apiVersionService.versionNoName, apiVersionService.versionNo)
        filterChain.doFilter(request, response)
    }

    companion object {
        /**
         * Higher order to ensure the filter is applied before Spring Security.
         */
        const val DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 1
    }
}
