package top.bettercode.summer.web.resolver

import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver
import top.bettercode.summer.web.error.ErrorAttributes
import top.bettercode.summer.web.error.IRespEntityConverter
import top.bettercode.summer.web.properties.SummerWebProperties

/**
 * @author Peter Wu
 */
class ApiExceptionHandlerExceptionResolver(private val summerWebProperties: SummerWebProperties,
                                           private val errorAttributes: ErrorAttributes,
                                           private val respEntityConverter: IRespEntityConverter?) : ExceptionHandlerExceptionResolver() {
    override fun afterPropertiesSet() {
        super.afterPropertiesSet()

        // Retrieve actual handlers to use as delegate
        val oldHandlers = returnValueHandlers!!

        // Set up ResourceProcessingHandlerMethodResolver to delegate to originally configured ones
        val newHandlers: MutableList<HandlerMethodReturnValueHandler> = ArrayList()
        newHandlers
                .add(ApiHandlerMethodReturnValueHandler(oldHandlers, summerWebProperties,
                        errorAttributes,
                        respEntityConverter))

        // Configure the new handler to be used
        setReturnValueHandlers(newHandlers)
    }
}
