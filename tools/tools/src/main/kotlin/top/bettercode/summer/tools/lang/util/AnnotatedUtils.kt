package top.bettercode.summer.tools.lang.util

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
            AnnotatedElementUtils.hasAnnotation(
                    handlerMethod.beanType,
                    annotationType
            )
        }
    }

    @JvmStatic
    fun <A : Annotation?> getAnnotation(
            handlerMethod: HandlerMethod,
            annotationType: Class<A>
    ): A? {
        val annotation = handlerMethod.getMethodAnnotation(annotationType)
        return annotation
                ?: AnnotatedElementUtils.getMergedAnnotation(
                        handlerMethod.beanType, annotationType
                )
    }

    @JvmStatic
    @JvmOverloads
    fun <A : Annotation?> getAnnotations(
            handlerMethod: HandlerMethod,
            annotationType: Class<A>,
            all: Boolean = false
    ): Set<A> {
        val annotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                handlerMethod.method, annotationType
        )
        return if (all) {
            val annotations1 = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                    handlerMethod.beanType, annotationType
            )
            annotations + annotations1
        } else {
            if (annotations.isEmpty()) AnnotatedElementUtils.getMergedRepeatableAnnotations(
                    handlerMethod.beanType, annotationType
            ) else annotations
        }
    }
}