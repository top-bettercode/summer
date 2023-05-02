package top.bettercode.summer.web.error

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.CorsProcessor
import org.springframework.web.cors.CorsUtils
import org.springframework.web.cors.DefaultCorsProcessor
import org.springframework.web.servlet.ModelAndView
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 自定义错误处理
 *
 * @author Peter Wu
 */
@ConditionalOnWebApplication
@ConditionalOnMissingBean(ErrorController::class)
@RequestMapping("\${server.error.path:\${error.path:/error}}")
class CustomErrorController(
        errorAttributes: ErrorAttributes?,
        errorProperties: ErrorProperties?,
        @param:Autowired(required = false) @param:Qualifier("corsConfigurationSource") private val configSource: CorsConfigurationSource?) : BasicErrorController(errorAttributes, errorProperties) {
    private val log = LoggerFactory.getLogger(CustomErrorController::class.java)
    private val processor: CorsProcessor = DefaultCorsProcessor()

    @Autowired(required = false)
    private val response: HttpServletResponse? = null
    @RequestMapping(produces = ["text/html"])
    override fun errorHtml(request: HttpServletRequest,
                           response: HttpServletResponse): ModelAndView {
        val model = Collections.unmodifiableMap(getErrorAttributes(
                request, getErrorAttributeOptions(request, MediaType.TEXT_HTML)))
        val status = getStatus(request)
        response.status = status.value()
        val modelAndView = resolveErrorView(request, response, status, model)
        setCors(request, response)
        return modelAndView ?: ModelAndView("error", model)
    }

    private fun setCors(request: HttpServletRequest, response: HttpServletResponse) {
        if (configSource != null && CorsUtils.isCorsRequest(request)) {
            val corsConfiguration = configSource.getCorsConfiguration(request)
            if (corsConfiguration != null) {
                try {
                    processor.processRequest(corsConfiguration, request, response)
                } catch (e: IOException) {
                    log.error("跨域设置出错", e)
                }
            }
        }
    }

    @RequestMapping
    @ResponseBody
    override fun error(request: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val body = getErrorAttributes(request,
                getErrorAttributeOptions(request, MediaType.ALL))
        val status = getStatus(request)
        val requestAttributes = RequestContextHolder
                .getRequestAttributes() as ServletRequestAttributes?
        if (requestAttributes != null) {
            setCors(request, requestAttributes.response!!)
        }
        response!!.status = status.value()
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        return ResponseEntity.status(status).headers(noCache()).body(body)
    }

    /**
     * @return 不支持客户端缓存，不支持客户端保存数据的响应头
     */
    protected fun noCache(): HttpHeaders {
        val headers = HttpHeaders()
        headers.cacheControl = "no-cache, no-store, max-age=0, must-revalidate"
        headers.pragma = "no-cache"
        headers.expires = -1
        return headers
    }
}