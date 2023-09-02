package top.bettercode.summer.web.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.cache.caffeine")
open class CaffeineCacheProperties {
    /**
     * 存储缓存配置,key:name ,value:spec [com.github.benmanes.caffeine.cache.CaffeineSpec]
     */
    var caches: Map<String, String>? = null
        private set

    /**
     * 默认缓存配置
     */
    var defaultSpec = "maximumSize=1000,expireAfterAccess=10s"
        private set

    fun setCaches(caches: Map<String, String>?): CaffeineCacheProperties {
        this.caches = caches
        return this
    }

    fun setDefaultSpec(defaultSpec: String): CaffeineCacheProperties {
        this.defaultSpec = defaultSpec
        return this
    }
}
