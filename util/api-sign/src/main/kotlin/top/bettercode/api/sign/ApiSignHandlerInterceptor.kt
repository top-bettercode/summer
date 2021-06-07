package top.bettercode.api.sign

import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.AsyncHandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 签名验证拦截器

 * @author Peter Wu
 */
class ApiSignHandlerInterceptor(
    private var apiSignAlgorithm: ApiSignAlgorithm,
) : AsyncHandlerInterceptor, MessageSourceAware, Ordered {

    private var messageSource: MessageSource? = null

    override fun setMessageSource(messageSource: MessageSource?) {
        this.messageSource = messageSource
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 20
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (apiSignAlgorithm.properties.requiredSign(handler)) {
            try {
                apiSignAlgorithm.checkSign(request)
            } catch (e: IllegalSignException) {
                val responseStatus = AnnotatedElementUtils.findMergedAnnotation(
                    e.javaClass,
                    ResponseStatus::class.java
                )
                if (responseStatus != null) {
                    val statusCode = responseStatus.code.value()
                    val reason = responseStatus.reason
                    if (!StringUtils.hasLength(reason)) {
                        response.sendError(statusCode)
                    } else {
                        val resolvedReason = if (this.messageSource != null) {
                            this.messageSource!!.getMessage(
                                reason,
                                null,
                                reason,
                                LocaleContextHolder.getLocale()
                            )
                        } else
                            reason
                        response.sendError(statusCode, resolvedReason)
                    }
                }
                return false
            }
        }

        return true
    }


}
