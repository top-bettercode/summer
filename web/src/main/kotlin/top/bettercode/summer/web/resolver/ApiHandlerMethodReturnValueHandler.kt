package top.bettercode.summer.web.resolver

import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
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

    @Throws(Exception::class)
    override fun handleReturnValue(returnValue: Any, returnType: MethodParameter,
                                   mavContainer: ModelAndViewContainer, webRequest: NativeWebRequest) {
        var returnValue = returnValue
        val typeContainingClass = returnType.containingClass
        val parameterType = returnType.parameterType
        if (Void.TYPE != parameterType
                && (AnnotatedElementUtils.hasAnnotation(typeContainingClass, ResponseBody::class.java)
                        || returnType.hasMethodAnnotation(ResponseBody::class.java)
                        || (HttpEntity::class.java.isAssignableFrom(parameterType)
                        && !RequestEntity::class.java.isAssignableFrom(parameterType)))) {

            //异常信息处理
            val isResponseEntity = returnValue is ResponseEntity<*>
            var body = if (isResponseEntity) (returnValue as ResponseEntity<*>).body else returnValue
            val nativeResponse = webRequest.getNativeResponse(HttpServletResponse::class.java)
            Assert.notNull(nativeResponse, "HttpServletResponse 为 null")
            if (body is Throwable) {
                val respEntity = errorAttributes.getErrorAttributes(body, webRequest)
                body = respEntity!!
                var statusCode = if (isResponseEntity) (returnValue as ResponseEntity<*>).statusCode else null
                val httpStatusCode = respEntity.httpStatusCode
                if (httpStatusCode != null) {
                    statusCode = HttpStatus.valueOf(httpStatusCode)
                }
                nativeResponse.status = statusCode!!.value()
                returnValue = if (isResponseEntity) {
                    ResponseEntity
                            .status(statusCode)
                            .headers((returnValue as ResponseEntity<*>).headers)
                            .body<Any>(body)
                } else {
                    body
                }
            }
            if (summerWebProperties.wrapEnable(webRequest) && !(returnValue is IRespEntity || returnValue is HttpEntity<*> && returnValue
                            .body is IRespEntity)
                    && supportsRewrapType(returnType)) {
                var value = returnValue
                if (returnValue is HttpEntity<*>) {
                    value = returnValue.body
                    returnValue = HttpEntity(rewrapResult(value),
                            returnValue.headers)
                } else {
                    returnValue = rewrapResult(value)
                }
            }
            if (summerWebProperties.okEnable(webRequest)) {
                nativeResponse.status = HttpStatus.OK.value()
                if (returnValue is ResponseEntity<*>) {
                    val statusCode = returnValue.statusCode.value()
                    if (statusCode != 404 && statusCode != 405) {
                        returnValue = ResponseEntity.ok()
                                .headers(returnValue.headers)
                                .body(returnValue.body)
                    }
                }
            }
        }
        delegate.handleReturnValue(returnValue, returnType, mavContainer, webRequest)
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

    private fun rewrapResult(originalValue: Any): Any {
        return RespEntity(originalValue)
    }
}
