package top.bettercode.summer.web.form

import org.springframework.web.method.HandlerMethod
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.getAnnotation
import top.bettercode.summer.web.servlet.NotErrorHandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 表单重复检
 *
 * @author Peter Wu
 */
class FormDuplicateCheckInterceptor(private val formkeyService: IFormkeyService, private val formKeyName: String) : NotErrorHandlerInterceptor {
    override fun preHandlerMethod(request: HttpServletRequest?, response: HttpServletResponse?,
                                  handler: HandlerMethod?): Boolean {
        val annotation = getAnnotation(handler!!, FormDuplicateCheck::class.java)
        return formkeyService.checkRequest(request, formKeyName, annotation != null, annotation?.expireSeconds
                ?: -1, annotation?.message ?: DEFAULT_MESSAGE)
    }

    override fun afterCompletionMethod(request: HttpServletRequest?, response: HttpServletResponse?, handler: HandlerMethod?, ex: Throwable?) {
        var ex = ex
        if (ex == null) {
            ex = getError(request!!)
        }
        if (ex != null) {
            formkeyService.cleanKey(request)
        }
    }

    companion object {
        val FORM_KEY = FormDuplicateCheckInterceptor::class.java.name + ".form_key"
        const val DEFAULT_MESSAGE = "您提交的太快了，请稍候再试。"
    }
}
