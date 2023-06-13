package top.bettercode.summer.web.resolver

import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.lang.Nullable
import org.springframework.util.Assert
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer
import top.bettercode.summer.web.IRespEntity
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.error.ErrorAttributes
import javax.servlet.http.HttpServletResponse

/**
 * @author Peter Wu
 */
class ApiHandlerMethodReturnValueHandler(
        private val delegate: HandlerMethodReturnValueHandler,
        private val summerWebProperties: SummerWebProperties, private val errorAttributes: ErrorAttributes) : HandlerMethodReturnValueHandler {
    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return delegate.supportsReturnType(returnType)
    }

    override fun handleReturnValue(@Nullable returnValue: Any?, returnType: MethodParameter, mavContainer: ModelAndViewContainer, webRequest: NativeWebRequest) {
        var returnVal = returnValue
        val typeContainingClass = returnType.containingClass
        val parameterType = returnType.parameterType
        if (Void.TYPE != parameterType
                && (AnnotatedElementUtils.hasAnnotation(typeContainingClass, ResponseBody::class.java)
                        || returnType.hasMethodAnnotation(ResponseBody::class.java)
                        || (HttpEntity::class.java.isAssignableFrom(parameterType)
                        && !RequestEntity::class.java.isAssignableFrom(parameterType)))) {

            //异常信息处理
            val isResponseEntity = returnVal is ResponseEntity<*>
            var body = if (isResponseEntity) (returnVal as ResponseEntity<*>).body else returnVal
            val nativeResponse = webRequest.getNativeResponse(HttpServletResponse::class.java)
            Assert.notNull(nativeResponse, "HttpServletResponse 为 null")
            if (body is Throwable) {
                val respEntity = errorAttributes.getErrorAttributes(body, webRequest)
                body = respEntity
                var statusCode = if (isResponseEntity) (returnVal as ResponseEntity<*>).statusCode else null
                val httpStatusCode = respEntity.httpStatusCode
                if (httpStatusCode != null) {
                    statusCode = HttpStatus.valueOf(httpStatusCode)
                }
                nativeResponse!!.status = statusCode!!.value()
                returnVal = if (isResponseEntity) {
                    ResponseEntity
                            .status(statusCode)
                            .headers((returnVal as ResponseEntity<*>).headers)
                            .body<Any>(body)
                } else {
                    body
                }
            }
            if (summerWebProperties.wrapEnable(webRequest) && !(returnVal is IRespEntity || returnVal is HttpEntity<*> && returnVal
                            .body is IRespEntity)
                    && supportsRewrapType(returnType)) {
                var value = returnVal
                if (returnVal is HttpEntity<*>) {
                    value = returnVal.body
                    returnVal = HttpEntity(rewrapResult(value),
                            returnVal.headers)
                } else {
                    returnVal = rewrapResult(value)
                }
            }
            if (summerWebProperties.okEnable(webRequest)) {
                nativeResponse!!.status = HttpStatus.OK.value()
                if (returnVal is ResponseEntity<*>) {
                    val statusCode = returnVal.statusCode.value()
                    if (statusCode != 404 && statusCode != 405) {
                        returnVal = ResponseEntity.ok()
                                .headers(returnVal.headers)
                                .body(returnVal.body)
                    }
                }
            }
        }
        delegate.handleReturnValue(returnVal, returnType, mavContainer, webRequest)
    }

    fun supportsRewrapType(returnType: MethodParameter): Boolean {
        val typeContainingClass = returnType.containingClass
        val parameterType = returnType.parameterType
        val support = (!AnnotatedElementUtils.hasAnnotation(parameterType, NoWrapResp::class.java)
                && !AnnotatedElementUtils.hasAnnotation(typeContainingClass, NoWrapResp::class.java)
                && !returnType.hasMethodAnnotation(NoWrapResp::class.java))
        return if (support) {
            returnType.executable.declaringClass.getPackage().name != "org.springframework.boot.actuate.endpoint.web.servlet"
        } else false
    }

    private fun rewrapResult(originalValue: Any?): Any {
        return RespEntity(originalValue)
    }
}
