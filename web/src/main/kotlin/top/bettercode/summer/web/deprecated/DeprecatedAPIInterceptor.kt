package top.bettercode.summer.web.deprecated

import org.springframework.context.MessageSource
import org.springframework.web.method.HandlerMethod
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.getAnnotation
import top.bettercode.summer.web.servlet.NotErrorHandlerInterceptor
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 已弃用的接口检查
 *
 * @author Peter Wu
 */
class DeprecatedAPIInterceptor(private val messageSource: MessageSource) : NotErrorHandlerInterceptor {
    override fun preHandlerMethod(request: HttpServletRequest?, response: HttpServletResponse?,
                                  handler: HandlerMethod?): Boolean {
        val annotation = getAnnotation(handler!!,
                DeprecatedAPI::class.java)
        check(annotation == null) { getText(request, annotation!!.message) }
        return true
    }

    private fun getText(request: HttpServletRequest?, code: Any, vararg args: Any): String {
        val codeString = code.toString()
        return messageSource.getMessage(codeString, args, codeString,
                if (request == null) Locale.CHINA else request.locale)?:""
    }
}
