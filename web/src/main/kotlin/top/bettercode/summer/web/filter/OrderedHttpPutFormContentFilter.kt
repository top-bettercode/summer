package top.bettercode.summer.web.filter

import org.springframework.core.Ordered
import org.springframework.http.HttpInputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

/**
 * 支持PUT DELETE form提交
 *
 * @author Peter Wu
 */
class OrderedHttpPutFormContentFilter : OncePerRequestFilter(), Ordered {
    private val formConverter: FormHttpMessageConverter = AllEncompassingFormHttpMessageConverter()
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

    /**
     * The default character set to use for reading form data.
     *
     * @param charset charset
     */
    fun setCharset(charset: Charset?) {
        formConverter.setCharset(charset)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        if (("PUT" == request.method || "DELETE" == request.method || ("PATCH"
                        == request.method)) && isFormContentType(request)) {
            val inputMessage: HttpInputMessage = object : ServletServerHttpRequest(request) {
                @Throws(IOException::class)
                override fun getBody(): InputStream {
                    return request.inputStream
                }
            }
            val formParameters = formConverter.read(null, inputMessage)
            val wrapper: HttpServletRequest = HttpPutFormContentRequestWrapper(request, formParameters)
            filterChain.doFilter(wrapper, response)
        } else {
            filterChain.doFilter(request, response)
        }
    }

    private fun isFormContentType(request: HttpServletRequest): Boolean {
        val contentType = request.contentType
        return if (contentType != null) {
            try {
                val mediaType = MediaType.parseMediaType(contentType)
                MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType)
            } catch (ex: IllegalArgumentException) {
                false
            }
        } else {
            false
        }
    }

    private class HttpPutFormContentRequestWrapper(request: HttpServletRequest?,
                                                   parameters: MultiValueMap<String, String>?) : HttpServletRequestWrapper(request) {
        private val formParameters: MultiValueMap<String, String>

        init {
            formParameters = parameters ?: LinkedMultiValueMap()
        }

        override fun getParameter(name: String): String {
            val queryStringValue = super.getParameter(name)
            val formValue = formParameters.getFirst(name)
            return queryStringValue ?: formValue
        }

        override fun getParameterMap(): Map<String, Array<String>> {
            val result: MutableMap<String, Array<String>> = LinkedHashMap()
            val names = this.parameterNames
            while (names.hasMoreElements()) {
                val name = names.nextElement()
                result[name] = getParameterValues(name)
            }
            return result
        }

        override fun getParameterNames(): Enumeration<String> {
            val names: MutableSet<String> = LinkedHashSet()
            names.addAll(Collections.list(super.getParameterNames()))
            names.addAll(formParameters.keys)
            return Collections.enumeration(names)
        }

        override fun getParameterValues(name: String): Array<String> {
            val queryStringValues = super.getParameterValues(name)
            val formValues = formParameters[name]
            return if (formValues == null) {
                queryStringValues
            } else if (queryStringValues == null) {
                formValues.toTypedArray<String>()
            } else {
                val result: MutableList<String> = ArrayList()
                result.addAll(Arrays.asList(*queryStringValues))
                result.addAll(formValues)
                result.toTypedArray<String>()
            }
        }
    }

    companion object {
        /**
         * Higher order to ensure the filter is applied before Spring Security.
         */
        const val DEFAULT_ORDER = -9900
    }
}
