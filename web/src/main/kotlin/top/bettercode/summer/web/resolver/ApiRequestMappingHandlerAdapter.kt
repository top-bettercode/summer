package top.bettercode.summer.web.resolver

import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import top.bettercode.summer.web.error.ErrorAttributes
import top.bettercode.summer.web.error.IRespEntityConverter
import top.bettercode.summer.web.properties.SummerWebProperties

/**
 * @author Peter Wu
 */
class ApiRequestMappingHandlerAdapter(private val summerWebProperties: SummerWebProperties,
                                      private val errorAttributes: ErrorAttributes,
                                      private val respEntityConverter: IRespEntityConverter?) : RequestMappingHandlerAdapter() {
    override fun afterPropertiesSet() {
        super.afterPropertiesSet()

        // Retrieve actual handlers to use as delegate
        val oldHandlers = HandlerMethodReturnValueHandlerComposite()
                .addHandlers(returnValueHandlers)

        // Set up ResourceProcessingHandlerMethodResolver to delegate to originally configured ones
        val newHandlers: MutableList<HandlerMethodReturnValueHandler> = ArrayList()
        newHandlers
                .add(ApiHandlerMethodReturnValueHandler(oldHandlers, summerWebProperties,
                        errorAttributes, respEntityConverter))

        // Configure the new handler to be used
        returnValueHandlers = newHandlers
    }
}
