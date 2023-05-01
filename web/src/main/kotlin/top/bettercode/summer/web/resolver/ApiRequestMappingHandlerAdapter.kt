package top.bettercode.summer.web.resolver

import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.error.ErrorAttributes

/**
 * @author Peter Wu
 */
class ApiRequestMappingHandlerAdapter(private val summerWebProperties: SummerWebProperties,
                                      private val errorAttributes: ErrorAttributes) : RequestMappingHandlerAdapter() {
    override fun afterPropertiesSet() {
        super.afterPropertiesSet()

        // Retrieve actual handlers to use as delegate
        val oldHandlers = HandlerMethodReturnValueHandlerComposite()
                .addHandlers(returnValueHandlers)

        // Set up ResourceProcessingHandlerMethodResolver to delegate to originally configured ones
        val newHandlers: MutableList<HandlerMethodReturnValueHandler> = ArrayList()
        newHandlers
                .add(ApiHandlerMethodReturnValueHandler(oldHandlers, summerWebProperties,
                        errorAttributes))

        // Configure the new handler to be used
        returnValueHandlers = newHandlers
    }
}
