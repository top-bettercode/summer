package top.bettercode.summer.web.xss

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

/**
 * XSS过滤处理
 */
class XssHttpServletRequestWrapper(
        /**
         * 获取最原始的request
         *
         * @return HttpServletRequest
         */
        //没被包装过的HttpServletRequest（特殊场景，需要自己过滤）
        val orgRequest: HttpServletRequest) : HttpServletRequestWrapper(orgRequest) {

    override fun getInputStream(): ServletInputStream {
        //非json类型，直接返回
        if (!MediaType.APPLICATION_JSON_VALUE
                        .equals(super.getHeader(HttpHeaders.CONTENT_TYPE), ignoreCase = true)) {
            return super.getInputStream()
        }

        //为空，直接返回
        var json = StreamUtils.copyToString(super.getInputStream(), StandardCharsets.UTF_8)
        if (json.isNullOrBlank()) {
            return super.getInputStream()
        }

        //xss过滤
        json = xssEncode(json)
        val bis = ByteArrayInputStream(
                json.toByteArray(StandardCharsets.UTF_8))
        return object : ServletInputStream() {
            override fun isFinished(): Boolean {
                return true
            }

            override fun isReady(): Boolean {
                return true
            }

            override fun setReadListener(readListener: ReadListener) {}
            override fun read(): Int {
                return bis.read()
            }
        }
    }

    override fun getParameter(name: String): String {
        var value = super.getParameter(xssEncode(name))
        if (!value.isNullOrBlank()) {
            value = xssEncode(value)
        }
        return value
    }

    override fun getParameterValues(name: String): Array<String>? {
        val parameters = super.getParameterValues(name)
        if (parameters == null || parameters.isEmpty()) {
            return null
        }
        for (i in parameters.indices) {
            parameters[i] = xssEncode(parameters[i])
        }
        return parameters
    }

    override fun getParameterMap(): Map<String, Array<String>> {
        val map: MutableMap<String, Array<String>> = LinkedHashMap()
        val parameters = super.getParameterMap()
        for (key in parameters.keys) {
            val values = parameters[key]!!
            for (i in values.indices) {
                values[i] = xssEncode(values[i])
            }
            map[key] = values
        }
        return map
    }

    override fun getHeader(name: String): String {
        var value = super.getHeader(xssEncode(name))
        if (!value.isNullOrBlank()) {
            value = xssEncode(value)
        }
        return value
    }

    private fun xssEncode(input: String): String {
        return htmlFilter.filter(input)
    }

    companion object {
        //html过滤
        private val htmlFilter = HTMLFilter()

        /**
         * @param request request
         * @return 获取最原始的request
         */
        fun getOrgRequest(request: HttpServletRequest): HttpServletRequest {
            return if (request is XssHttpServletRequestWrapper) {
                request.orgRequest
            } else request
        }
    }
}
