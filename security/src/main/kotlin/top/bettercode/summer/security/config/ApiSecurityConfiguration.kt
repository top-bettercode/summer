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
import top.bettercode.summer.security.ApiTokenService
import top.bettercode.summer.security.IResourceService
import top.bettercode.summer.security.client.ClientDetailsService
import top.bettercode.summer.security.repository.InMemoryStoreTokenRepository
import top.bettercode.summer.security.repository.StoreTokenRepository
import top.bettercode.summer.security.support.ApiSecurityErrorHandler
import top.bettercode.summer.security.token.AccessTokenConverter
import top.bettercode.summer.security.token.DefaulAccessTokenConverter
import top.bettercode.summer.security.token.StoreToken
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest
import kotlin.math.max

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties(ApiSecurityProperties::class)
class ApiSecurityConfiguration(
        private val securityProperties: ApiSecurityProperties
) {
    @ConditionalOnMissingBean(IResourceService::class)
    @Bean
    fun resourceService(): IResourceService {
        return object : IResourceService {}
    }

    @ConditionalOnMissingBean(AccessTokenConverter::class)
    @Bean
    fun accessTokenConverter(): AccessTokenConverter {
        return DefaulAccessTokenConverter()
    }

    @ConditionalOnMissingBean(ClientDetailsService::class)
    @Bean
    fun clientDetailsService(): ClientDetailsService {
        return ClientDetailsService(listOf(securityProperties))
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
    fun apiTokenService(
            storeTokenRepository: StoreTokenRepository,
            clientDetailsService: ClientDetailsService,
            accessTokenConverter: AccessTokenConverter,
            userDetailsService: UserDetailsService
    ): ApiTokenService {
        return ApiTokenService(storeTokenRepository, clientDetailsService, accessTokenConverter, userDetailsService)
    }

    @Bean
    fun apiSecurityErrorHandler(
            messageSource: MessageSource,
            @Autowired(required = false) request: HttpServletRequest?
    ): ApiSecurityErrorHandler {
        return ApiSecurityErrorHandler(messageSource, request)
    }

    @ConditionalOnMissingBean(StoreTokenRepository::class)
    @Bean
    fun storeTokenRepository(clientDetailsService: ClientDetailsService): StoreTokenRepository {
        val cache = Caffeine.newBuilder()
                .expireAfterWrite(max(clientDetailsService.maxAccessTokenValiditySeconds,
                        clientDetailsService.maxRefreshTokenValiditySeconds).toLong(), TimeUnit.SECONDS)
                .maximumSize(10000).build<String, StoreToken>()
        val accessTokenBuild = Caffeine.newBuilder()
                .expireAfterWrite(clientDetailsService.maxAccessTokenValiditySeconds.toLong(), TimeUnit.SECONDS)
                .maximumSize(10000).build<String, String>()
        val refreshTokenBuild = Caffeine.newBuilder()
                .expireAfterWrite(
                        clientDetailsService.maxRefreshTokenValiditySeconds.toLong(), TimeUnit.SECONDS)
                .maximumSize(10000).build<String, String>()
        return InMemoryStoreTokenRepository(cache.asMap(), accessTokenBuild.asMap(),
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