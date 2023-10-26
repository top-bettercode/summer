package top.bettercode.summer.logging

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.util.ClassUtils
import org.springframework.web.method.HandlerMethod

/**
 *
 * @author Peter Wu
 */
object SwaggerUtil {

    fun getCollectionName(handler: HandlerMethod): String? {
        return if (ClassUtils.isPresent("io.swagger.annotations.Api", SwaggerUtil::class.java.classLoader)) {
            AnnotatedElementUtils.getMergedAnnotation(
                    handler.beanType,
                    io.swagger.annotations.Api::class.java
            )?.value
        } else {
            null
        }
    }

    fun getOperationName(handler: HandlerMethod): String? {
        return if (ClassUtils.isPresent("io.swagger.annotations.ApiOperation", SwaggerUtil::class.java.classLoader)) {
            handler.getMethodAnnotation(io.swagger.annotations.ApiOperation::class.java)?.value
        } else {
            null
        }
    }

}