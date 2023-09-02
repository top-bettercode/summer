package top.bettercode.summer.web.form

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.operation.RequestConverter.getRequestWrapper
import top.bettercode.summer.tools.lang.trace.TraceHttpServletRequestWrapper
import top.bettercode.summer.tools.lang.util.Sha512DigestUtils.shaHex
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import java.time.Duration
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
interface IFormkeyService {

    fun checkRequest(request: HttpServletRequest?, formKeyName: String?, autoFormKey: Boolean, ttl: Duration?, message: String?): Boolean {
        val formkey = getFormkey(request, formKeyName, autoFormKey)
        return checkRequest(request = request, formkey = formkey, ttl = ttl, message = message)
    }

        fun checkRequest(request: HttpServletRequest?, formKeyName: String?, autoFormKey: Boolean, ttl: Duration?, message: String?, ignoreHeaders: Array<String>? = null, ignoreParams: Array<String>? = null): Boolean {
        val formkey = getFormkey(request, formKeyName, autoFormKey, ignoreHeaders, ignoreParams)
        return checkRequest(request = request, formkey = formkey, ttl = ttl, message = message)
    }

    fun checkRequest(request: HttpServletRequest?, formkey: String?, ttl: Duration?, message: String?): Boolean {
        return if (formkey == null) {
            true
        } else if (exist(formkey, ttl)) {
            throw FormDuplicateException(message ?: FormDuplicateCheckInterceptor.DEFAULT_MESSAGE)
        } else {
            request!!.setAttribute(FormDuplicateCheckInterceptor.FORM_KEY, formkey)
            true
        }
    }

    fun cleanKey(request: HttpServletRequest) {
        val formkey = request.getAttribute(FormDuplicateCheckInterceptor.FORM_KEY) as String?
        if (formkey != null) {
            remove(formkey)
        }
        if (log.isTraceEnabled) {
            log.trace("{} remove:{}", request.requestURI, formkey)
        }
    }

    fun getFormkey(request: HttpServletRequest?, formKeyName: String?, autoFormKey: Boolean): String? {
        return getFormkey(request, formKeyName, autoFormKey, null, null)
    }

    fun getFormkey(request: HttpServletRequest?, formKeyName: String?, autoFormKey: Boolean, ignoreHeaders: Array<String>?, ignoreParams: Array<String>?): String? {
        var digestFormkey: String? = null
        var formkey = request!!.getHeader(formKeyName)
        val hasFormKey = StringUtils.hasText(formkey)
        if (hasFormKey || autoFormKey) {
            if (log.isTraceEnabled) {
                log.trace(request.servletPath + " formDuplicateCheck")
            }
            if (!hasFormKey) {
                val servletServerHttpRequest = ServletServerHttpRequest(
                        request)
                val httpHeaders: MultiValueMap<String, String> = LinkedMultiValueMap(servletServerHttpRequest.headers)
                if (ignoreHeaders != null) {
                    for (ignoreHeader in ignoreHeaders) {
                        httpHeaders.remove(ignoreHeader)
                    }
                }
                formkey = valueOf(httpHeaders)
                val parameterMap: MutableMap<String, Array<String>> = HashMap(request.parameterMap)
                if (ignoreParams != null) {
                    for (ignoreParam in ignoreParams) {
                        parameterMap.remove(ignoreParam)
                    }
                }
                val params = valueOf(parameterMap)
                formkey += "::$params"
                val contentType = request.contentType
                val formPost = (contentType != null && contentType.contains("application/x-www-form-urlencoded")
                        && HttpMethod.POST.matches(request.method))
                if (!formPost) {
                    val traceHttpServletRequestWrapper = getRequestWrapper(
                            request, TraceHttpServletRequestWrapper::class.java)
                    formkey += if (traceHttpServletRequestWrapper != null) {
                        try {
                            "::" + traceHttpServletRequestWrapper.content
                        } catch (e: Exception) {
                            log.info(
                                    request.servletPath + e.message + " ignore formDuplicateCheck")
                            return null
                        }
                    } else {
                        log.info(request.servletPath
                                + " not traceHttpServletRequestWrapper ignore formDuplicateCheck")
                        return null
                    }
                }
            }
            formkey = formkey + request.method + request.requestURI
            digestFormkey = shaHex(formkey)
            if (log.isTraceEnabled) {
                log.trace("{} formkey:{},digestFormkey:{}", request.requestURI, formkey,
                        digestFormkey)
            }
        }
        return digestFormkey
    }

    fun exist(formkey: String, ttl: Duration?): Boolean
    fun remove(formkey: String)

    companion object {
        val log: Logger = LoggerFactory.getLogger(IFormkeyService::class.java)
    }
}
