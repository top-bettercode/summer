package top.bettercode.summer.test

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.util.Base64Utils
import top.bettercode.summer.security.authorize.Anonymous
import top.bettercode.summer.security.authorize.ClientAuthorize
import top.bettercode.summer.security.config.ApiSecurityProperties
import top.bettercode.summer.security.support.AuthenticationHelper
import top.bettercode.summer.security.support.SecurityParameterNames
import top.bettercode.summer.test.autodoc.Autodoc
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.hasAnnotation
import top.bettercode.summer.web.servlet.HandlerMethodContextHolder.getHandler

@ConditionalOnBean(ApiSecurityProperties::class)
@Configuration(proxyBeanMethods = false)
class AutodocAuthWebMvcConfigurer(
        private val securityProperties: ApiSecurityProperties) : AutoDocRequestHandler {

    @Bean
    fun testAuthenticationService(userDetailsService: UserDetailsService): TestAuthenticationService {
        return DefaultTestAuthenticationService(userDetailsService)
    }

    override fun handle(request: AutoDocHttpServletRequest) {
        val handler = getHandler(request)
        if (handler != null) {
            val username = AuthenticationHelper.username
            username.ifPresent { request.setAttribute(HttpOperation.REQUEST_LOGGING_USERNAME, username.get()) }

            if (Autodoc.requireAuthorization) {
                Autodoc.requiredHeaders(HttpHeaders.AUTHORIZATION)
            }

            var requiredHeaders = Autodoc.requiredHeaders
            val url = request.requestURI
            var needAuth = false
            //set required
            val isClientAuth = hasAnnotation(handler, ClientAuthorize::class.java)
            if (!hasAnnotation(handler, Anonymous::class.java)
                    && !securityProperties.ignored(url) || isClientAuth) {
                requiredHeaders = HashSet(requiredHeaders)
                if (securityProperties.isCompatibleAccessToken) {
                    requiredHeaders.add(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN)
                } else {
                    requiredHeaders.add(HttpHeaders.AUTHORIZATION)
                }
                needAuth = true
                Autodoc.requiredHeaders(*requiredHeaders.toTypedArray<String>())
                //set required end
            }

            if (Autodoc.requiredHeaders.contains(HttpHeaders.AUTHORIZATION)) {
                needAuth = true
            }
            if (needAuth) {
                if (isClientAuth && !Autodoc.requireAuthorization) {
                    request.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString(
                            (securityProperties.clientId + ":"
                                    + securityProperties.clientSecret).toByteArray()))
                } else {
                    if (securityProperties.isCompatibleAccessToken) {
                        val authorization = request.getHeader(
                                SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN)
                        if (authorization.isNullOrBlank()) {
                            request.header(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN,
                                    "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                        }
                    } else {
                        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
                        if (authorization.isNullOrBlank()) {
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