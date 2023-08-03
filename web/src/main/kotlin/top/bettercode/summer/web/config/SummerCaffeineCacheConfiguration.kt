package top.bettercode.summer.web.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.bettercode.summer.web.properties.CaffeineCacheProperties

/**
 * CaffeineCache 配置
 *
 * @author Peter Wu
 */
@ConditionalOnClass(CaffeineCacheManager::class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CaffeineCacheProperties::class)
class SummerCaffeineCacheConfiguration {
    @Bean
    fun caffeineCacheManagerCustomizer(
            caffeineCacheProperties: CaffeineCacheProperties): CacheManagerCustomizer<CaffeineCacheManager> {
        return CacheManagerCustomizer { cacheManager: CaffeineCacheManager ->
            val caches = caffeineCacheProperties.caches
            if (caches != null) {
                caches.forEach { (name: String, spec: String) ->
                    cacheManager.registerCustomCache(name,
                            Caffeine.from(spec).build())
                }
            } else {
                cacheManager.setCacheSpecification(caffeineCacheProperties.defaultSpec)
            }
        }
    }
}
