package top.bettercode.summer.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import top.bettercode.summer.security.ApiTokenEndpointFilter
import top.bettercode.summer.security.ApiTokenService
import top.bettercode.summer.security.IResourceService
import top.bettercode.summer.security.authorization.RequestMappingAuthorizationManager
import top.bettercode.summer.security.token.IRevokeTokenService
import top.bettercode.summer.web.form.FormkeyService
import top.bettercode.summer.web.properties.CorsProperties
import top.bettercode.summer.web.properties.SummerWebProperties
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "summer.security", name = ["enabled"], matchIfMissing = true)
@EnableConfigurationProperties(CorsProperties::class)
class SecurityConfiguration(
    private val corsProperties: CorsProperties,
    private val securityProperties: ApiSecurityProperties,
    private val apiTokenService: ApiTokenService,
    @param:Autowired(required = false) private val revokeTokenService: IRevokeTokenService?,
    private val summerWebProperties: SummerWebProperties,
    private val objectMapper: ObjectMapper,
    private val passwordEncoder: PasswordEncoder,
    private val formkeyService: FormkeyService,
) {
    @Bean
    fun authorizationManager(
        resourceService: IResourceService,
        requestMappingHandlerMapping: RequestMappingHandlerMapping,
    ): RequestMappingAuthorizationManager {
        return RequestMappingAuthorizationManager(
            resourceService, requestMappingHandlerMapping,
            securityProperties
        )
    }

    @Bean
    fun configure(
        http: HttpSecurity,
        access: AuthorizationManager<RequestAuthorizationContext>,
    ): SecurityFilterChain {
        http.headers { headersConfigurer ->
            if (securityProperties.isSupportClientCache) {
                headersConfigurer.cacheControl {
                    it.disable()
                }
            }
            if (securityProperties.isFrameOptionsDisable) {
                headersConfigurer.frameOptions {
                    it.disable()
                }
            }
        }
        if (corsProperties.isEnable) {
            http.cors(Customizer.withDefaults())
        }
        http.csrf {
            it.disable()
        }
        val apiTokenEndpointFilter = ApiTokenEndpointFilter(
            apiTokenService,
            passwordEncoder, summerWebProperties, revokeTokenService, objectMapper, formkeyService
        )
        http.addFilterBefore(apiTokenEndpointFilter, LogoutFilter::class.java)
        http.sessionManagement {
            it.sessionCreationPolicy(securityProperties.sessionCreationPolicy)
        }
            .exceptionHandling {
                it.accessDeniedHandler { _: HttpServletRequest, _: HttpServletResponse, accessDeniedException: AccessDeniedException ->
                    throw accessDeniedException
                }
                it.authenticationEntryPoint { _: HttpServletRequest, _: HttpServletResponse, authException: AuthenticationException ->
                    throw authException
                }
            }
            .authorizeHttpRequests {
                it.anyRequest().access(access)
            }
        return http.build()
    }
}