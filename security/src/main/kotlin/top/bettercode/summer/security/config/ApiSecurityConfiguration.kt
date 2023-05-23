package top.bettercode.summer.security.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import top.bettercode.summer.security.ApiTokenService
import top.bettercode.summer.security.IResourceService
import top.bettercode.summer.security.authorization.URLFilterInvocationSecurityMetadataSource
import top.bettercode.summer.security.repository.ApiTokenRepository
import top.bettercode.summer.security.repository.InMemoryApiTokenRepository
import top.bettercode.summer.security.support.ApiSecurityErrorHandler
import top.bettercode.summer.security.token.ApiToken
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties(ApiSecurityProperties::class)
class ApiSecurityConfiguration(
        private val securityProperties: ApiSecurityProperties) {
    @Bean
    fun securityMetadataSource(
            resourceService: IResourceService,
            requestMappingHandlerMapping: RequestMappingHandlerMapping): URLFilterInvocationSecurityMetadataSource {
        return URLFilterInvocationSecurityMetadataSource(resourceService,
                requestMappingHandlerMapping, securityProperties)
    }

    @ConditionalOnMissingBean(IResourceService::class)
    @Bean
    fun resourceService(): IResourceService {
        return object : IResourceService {}
    }

    @ConditionalOnMissingBean(PasswordEncoder::class)
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val secureRandomSeed = securityProperties.secureRandomSeed
        if (!secureRandomSeed.isNullOrBlank()) {
            return BCryptPasswordEncoder(-1, SecureRandom(secureRandomSeed.toByteArray()))
        }
        return BCryptPasswordEncoder()
    }

    @Bean
    fun apiTokenService(apiAuthorizationService: ApiTokenRepository,
                        userDetailsService: UserDetailsService): ApiTokenService {
        return ApiTokenService(securityProperties, apiAuthorizationService, userDetailsService)
    }

    @Bean
    fun securityOAuth2ErrorHandler(messageSource: MessageSource,
                                   @Autowired(required = false) request: HttpServletRequest?): ApiSecurityErrorHandler {
        return ApiSecurityErrorHandler(messageSource, request)
    }

    @ConditionalOnMissingBean(ApiTokenRepository::class)
    @Bean
    fun apiAuthorizationService(): ApiTokenRepository {
        val cache = Caffeine.newBuilder()
                .expireAfterWrite(Math.max(securityProperties.accessTokenValiditySeconds,
                        securityProperties.refreshTokenValiditySeconds).toLong(), TimeUnit.SECONDS)
                .maximumSize(10000).build<String, ApiToken>()
        val accessTokenBuild = Caffeine.newBuilder()
                .expireAfterWrite(securityProperties.accessTokenValiditySeconds.toLong(), TimeUnit.SECONDS)
                .maximumSize(10000).build<String, String>()
        val refreshTokenBuild = Caffeine.newBuilder()
                .expireAfterWrite(
                        securityProperties.refreshTokenValiditySeconds.toLong(), TimeUnit.SECONDS)
                .maximumSize(10000).build<String, String>()
        return InMemoryApiTokenRepository(cache.asMap(), accessTokenBuild.asMap(),
                refreshTokenBuild.asMap())
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication
    protected class ObjectMapperBuilderCustomizer : Jackson2ObjectMapperBuilderCustomizer {
        override fun customize(jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder) {
            jacksonObjectMapperBuilder.serializerByType(GrantedAuthority::class.java,
                    object : JsonSerializer<GrantedAuthority>() {
                        override fun serialize(
                                value: GrantedAuthority, gen: JsonGenerator,
                                serializers: SerializerProvider
                        ) {
                            gen.writeString(value.authority)
                        }
                    })
        }
    }
}