package top.bettercode.summer.test

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.util.Base64Utils
import org.springframework.util.StringUtils
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.security.authorize.ClientAuthorize
import top.bettercode.summer.security.config.ApiSecurityProperties
import top.bettercode.summer.security.support.AuthenticationHelper
import top.bettercode.summer.security.support.SecurityParameterNames
import top.bettercode.summer.test.autodoc.Autodoc.requiredHeaders
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.hasAnnotation
import top.bettercode.summer.web.servlet.HandlerMethodContextHolder.getHandler

@ConditionalOnBean(ApiSecurityProperties::class)
@Configuration(proxyBeanMethods = false)
class AutodocAuthWebMvcConfigurer(
        private val securityProperties: ApiSecurityProperties) : AutoDocRequestHandler {
    override fun handle(request: AutoDocHttpServletRequest) {
        val handler = getHandler(request)
        if (handler != null) {
            val username = AuthenticationHelper.username
            username.ifPresent { request.setAttribute(HttpOperation.REQUEST_LOGGING_USERNAME, username.get()) }
            var requiredHeaders = requiredHeaders
            val url = request.requestURI
            var needAuth = false
            //set required
            val isClientAuth = hasAnnotation(handler, ClientAuthorize::class.java)
            if (!hasAnnotation<Anonymous>(handler, Anonymous::class.java)
                    && !securityProperties.ignored(url) || isClientAuth) {
                requiredHeaders = HashSet(requiredHeaders)
                if (securityProperties.isCompatibleAccessToken) {
                    requiredHeaders.add(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN)
                } else {
                    requiredHeaders.add(HttpHeaders.AUTHORIZATION)
                }
                needAuth = true
                requiredHeaders(*requiredHeaders.toTypedArray<String>())
                //set required end
            }
            if (needAuth) {
                if (isClientAuth) {
                    request.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString(
                            (securityProperties.clientId + ":"
                                    + securityProperties.clientSecret).toByteArray()))
                } else {
                    if (securityProperties.isCompatibleAccessToken) {
                        val authorization = request.getHeader(
                                SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN)
                        if (!StringUtils.hasText(authorization)) {
                            request.header(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN,
                                    "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                        }
                    } else {
                        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
                        if (!StringUtils.hasText(authorization)) {
                            request.header(HttpHeaders.AUTHORIZATION,
                                    "bearer xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                        }
                    }
                }
            }
        }
    }

    override fun support(request: AutoDocHttpServletRequest): Boolean {
        return request.isMock()
    }
}