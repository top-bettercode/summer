package top.bettercode.summer.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.security.access.AccessDecisionManager
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.ConfigAttribute
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.ObjectPostProcessor
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor
import org.springframework.security.web.authentication.logout.LogoutFilter
import top.bettercode.summer.security.ApiTokenEndpointFilter
import top.bettercode.summer.security.ApiTokenService
import top.bettercode.summer.security.authorization.URLFilterInvocationSecurityMetadataSource
import top.bettercode.summer.security.authorization.UserDetailsAuthenticationProvider
import top.bettercode.summer.security.token.IRevokeTokenService
import top.bettercode.summer.web.form.IFormkeyService
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
    private val formkeyService: IFormkeyService,
    private val accessDecisionManager: AccessDecisionManager,
    private val securityMetadataSource: URLFilterInvocationSecurityMetadataSource,
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
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
        http.authenticationProvider(UserDetailsAuthenticationProvider())
        http.addFilterBefore(apiTokenEndpointFilter, LogoutFilter::class.java)
        http
            .sessionManagement {
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
            .authorizeRequests {
                it.withObjectPostProcessor(object : ObjectPostProcessor<FilterSecurityInterceptor> {
                    override fun <O : FilterSecurityInterceptor> postProcess(
                        fsi: O
                    ): O {
                        fsi.securityMetadataSource = securityMetadataSource
                        fsi.accessDecisionManager = accessDecisionManager
                        return fsi
                    }
                })
                    .anyRequest().authenticated()
            }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication
    protected class AccessSecurityConfiguration {
        private val log = LoggerFactory.getLogger(
            SecurityConfiguration::class.java
        )

        @ConditionalOnMissingBean
        @Bean
        fun accessDecisionManager(): AccessDecisionManager {
            return object : AccessDecisionManager {
                override fun decide(
                    authentication: Authentication, `object`: Any,
                    configAttributes: Collection<ConfigAttribute>
                ) {
                    val authorities: Collection<GrantedAuthority> = authentication.authorities
                    if (log.isDebugEnabled) {
                        log.debug("权限检查，当前用户权限：{}，当前资源需要以下权限之一：{}",
                            authorities.joinToString(),
                            configAttributes.joinToString { obj: ConfigAttribute -> obj.attribute })
                    }
                    for (configAttribute in configAttributes) { //需要的权限，有任意其中一个即可
                        if (contains(authorities, configAttribute)) {
                            return
                        }
                    }
                    log.info("权限检查，当前用户权限：{}，当前资源需要以下权限之一：{}",
                        authorities.joinToString(),
                        configAttributes.joinToString { obj: ConfigAttribute -> obj.attribute })
                    throw AccessDeniedException("无权访问")
                }

                private fun contains(
                    authorities: Collection<GrantedAuthority>,
                    attribute: ConfigAttribute
                ): Boolean {
                    val attributeAttribute: String = attribute.attribute
                    for (authority in authorities) {
                        if (attributeAttribute == authority.authority) {
                            return true
                        }
                    }
                    return false
                }

                override fun supports(attribute: ConfigAttribute): Boolean {
                    return true
                }

                override fun supports(clazz: Class<*>): Boolean {
                    return true
                }
            }
        }
    }
}