package top.bettercode.summer.web.form

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import top.bettercode.summer.tools.lang.operation.RequestConverter.getRequestWrapper
import top.bettercode.summer.tools.lang.trace.TraceHttpServletRequestWrapper
import top.bettercode.summer.tools.lang.util.Sha512DigestUtils.shaHex
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import java.time.Duration
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
abstract class FormkeyService {

    fun <T> duplicateCheck(
        request: HttpServletRequest,
        formKeyName: String,
        autoFormKey: Boolean = true,
        ttl: Duration? = null,
        message: String? = null,
        ignoreHeaders: Array<String>? = null,
        ignoreParams: Array<String>? = null,
        runnable: (() -> T?)? = null
    ): T? {
        val formkey = getFormkey(request, formKeyName, autoFormKey, ignoreHeaders, ignoreParams)
        if (formkey != null && runnable == null) {
            request.setAttribute(FormDuplicateCheckInterceptor.FORM_KEY, formkey)
        }
        return duplicateCheck(formkey, ttl, message, runnable ?: { null })
    }

    fun <T> duplicateCheck(
        formkey: String?,
        ttl: Duration? = null,
        message: String? = null,
        runnable: Runnable
    ) {
        duplicateCheck(formkey, ttl, message) {
            runnable.run()
            null
        }
    }

    fun <T> duplicateCheck(
        formkey: String?,
        ttl: Duration? = null,
        message: String? = null,
        runnable: () -> T?
    ): T? {
        if (formkey != null && exist(formkey, ttl)) {
            throw FormDuplicateException(message ?: FormDuplicateCheckInterceptor.DEFAULT_MESSAGE)
        } else {
            return try {
                runnable()
            } catch (ex: Exception) {
                if (formkey != null) {
                    remove(formkey)
                }
                throw ex
            }
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

    fun getFormkey(
        request: HttpServletRequest,
        formKeyName: String,
        autoFormKey: Boolean,
        ignoreHeaders: Array<String>? = null,
        ignoreParams: Array<String>? = null
    ): String? {
        var digestFormkey: String? = null
        var formkey = request.getHeader(formKeyName)
        val hasFormKey = !formkey.isNullOrBlank()
        if (hasFormKey || autoFormKey) {
            if (log.isTraceEnabled) {
                log.trace(request.servletPath + " formDuplicateCheck")
            }
            if (!hasFormKey) {
                val servletServerHttpRequest = ServletServerHttpRequest(
                    request
                )
                val httpHeaders: MultiValueMap<String, String> =
                    LinkedMultiValueMap(servletServerHttpRequest.headers)
                if (!ignoreHeaders.isNullOrEmpty()) {
                    for (ignoreHeader in ignoreHeaders) {
                        httpHeaders.remove(ignoreHeader)
                    }
                }
                formkey = valueOf(httpHeaders)
                val parameterMap: MutableMap<String, Array<String>> = HashMap(request.parameterMap)
                if (!ignoreParams.isNullOrEmpty()) {
                    for (ignoreParam in ignoreParams) {
                        parameterMap.remove(ignoreParam)
                    }
                }
                val params = valueOf(parameterMap)
                formkey += "::$params"
                val contentType = request.contentType
                val formPost =
                    (contentType != null && contentType.contains("application/x-www-form-urlencoded")
                            && HttpMethod.POST.matches(request.method))
                if (!formPost) {
                    val traceHttpServletRequestWrapper = getRequestWrapper(
                        request, TraceHttpServletRequestWrapper::class.java
                    )
                    formkey += if (traceHttpServletRequestWrapper != null) {
                        try {
                            "::" + traceHttpServletRequestWrapper.content
                        } catch (e: Exception) {
                            log.info(request.servletPath + e.message + " ignore formDuplicateCheck")
                            null
                        }
                    } else {
                        log.info(request.servletPath + " not traceHttpServletRequestWrapper ignore formDuplicateCheck")
                        null
                    }
                }
            }
            formkey = formkey + request.method + request.requestURI
            digestFormkey = shaHex(formkey)
            if (log.isTraceEnabled) {
                log.trace(
                    "{} formkey:{},digestFormkey:{}", request.requestURI, formkey,
                    digestFormkey
                )
            }
        }
        return digestFormkey
    }

    @JvmOverloads
    fun runIfAbsent(
        formkey: String,
        ttl: Duration?,
        waitTime: Duration? = null,
        runnable: Runnable
    ) {
        runIfAbsent(formkey = formkey, waitTime = waitTime, ttl = ttl) {
            runnable.run()
        }
    }

    @JvmOverloads
    fun <T> runIfAbsent(
        formkey: String,
        ttl: Duration?,
        waitTime: Duration? = null,
        runnable: () -> T?
    ): T? {
        return if (!exist(formkey, ttl)) {
            try {
                runnable()
            } finally {
                remove(formkey)
            }
        } else {
            if (waitTime != null && waitTime.toMillis() > 0) {
                Thread.sleep(DURATION)
                runIfAbsent(
                    formkey = formkey,
                    waitTime = waitTime.minusMillis(DURATION),
                    ttl = ttl
                ) {
                    runnable()
                }
            } else {
                log.info("formkey:{} exist", formkey)
                null
            }
        }
    }

    @JvmOverloads
    fun runIfAbsentOrElseThrow(
        formkey: String,
        ttl: Duration? = null,
        expMsg: String? = null,
        waitTime: Duration? = null,
        runnable: Runnable
    ) {
        runIfAbsentOrElseThrow(formkey = formkey, waitTime = waitTime, ttl = ttl, expMsg = expMsg) {
            runnable.run()
        }
    }

    @JvmOverloads
    fun <T> runIfAbsentOrElseThrow(
        formkey: String,
        ttl: Duration? = null,
        expMsg: String? = null,
        waitTime: Duration? = null,
        runnable: () -> T?
    ): T? {
        return if (!exist(formkey, ttl)) {
            try {
                runnable()
            } finally {
                remove(formkey)
            }
        } else {
            if (waitTime != null && waitTime.toMillis() > 0) {
                Thread.sleep(DURATION)
                runIfAbsentOrElseThrow(
                    formkey = formkey,
                    ttl = ttl,
                    expMsg = expMsg,
                    waitTime = waitTime.minusMillis(DURATION),
                    runnable = runnable
                )
            } else {
                throw IllegalStateException(expMsg ?: FormDuplicateCheckInterceptor.DEFAULT_MESSAGE)
            }
        }
    }

    abstract fun exist(formkey: String, ttl: Duration?): Boolean
    abstract fun remove(formkey: String)

    companion object {
        private val log: Logger = LoggerFactory.getLogger(FormkeyService::class.java)
        private const val DURATION: Long = 200
    }
}
