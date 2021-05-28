package cn.bestwu.logging

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.method.HandlerMethod

/**
 * @author Peter Wu
 */
object AnnotatedUtils {

    @JvmStatic
    fun <A : Annotation?> hasAnnotation(
        handlerMethod: HandlerMethod,
        annotationType: Class<A>
    ): Boolean {
        return if (handlerMethod.hasMethodAnnotation(annotationType)) {
            true
        } else {
            AnnotatedElementUtils.hasAnnotation(handlerMethod.beanType, annotationType)
        }
    }

    @JvmStatic
    fun <A : Annotation?> getAnnotation(
        handlerMethod: HandlerMethod,
        annotationType: Class<A>
    ): A {
        return handlerMethod.getMethodAnnotation(annotationType)
            ?: handlerMethod.beanType.getAnnotation(annotationType)
    }
}