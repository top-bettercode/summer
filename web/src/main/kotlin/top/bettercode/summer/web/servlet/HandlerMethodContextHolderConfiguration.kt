package top.bettercode.summer.web.servlet

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@ConditionalOnBean(RequestMappingHandlerMapping::class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
class HandlerMethodContextHolderConfiguration(
        requestMappingHandlerMapping: RequestMappingHandlerMapping) {
    init {
        HandlerMethodContextHolder.setHandlerMapping(requestMappingHandlerMapping)
    }
}