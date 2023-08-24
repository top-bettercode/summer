package top.bettercode.summer.tools.weixin.support

import com.fasterxml.jackson.annotation.JsonInclude
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.getForObject
import top.bettercode.summer.tools.weixin.properties.IWexinProperties
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.BasicAccessToken
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.CachedValue
import top.bettercode.summer.web.support.client.ApiTemplate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
open class WeixinClient<T : IWexinProperties>(
        val properties: T,
        collectionName: String,
        name: String,
        logMarker: String
) :
        ApiTemplate(
                collectionName,
                name,
                logMarker,
                properties.connectTimeout,
                properties.readTimeout
        ) {

    private var lastAppId = properties.appId

    companion object {
        const val BASE_ACCESS_TOKEN_KEY: String = "access_token"
    }

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
                object : MappingJackson2HttpMessageConverter() {
                    override fun canRead(mediaType: MediaType?): Boolean {
                        return true
                    }

                    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
                        return true
                    }
                }
        val objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(messageConverter)
        super.setMessageConverters(messageConverters)
    }

    private val cache: Cache<String, CachedValue> by lazy {
        Caffeine.newBuilder().expireAfterWrite(properties.cacheSeconds, TimeUnit.SECONDS)
                .maximumSize(1000).build()
    }

    protected fun getFromCacheIfPresent(key: String): String? {
        val cachedValue = cache.getIfPresent(key)
        return if (cachedValue == null || cachedValue.expired) {
            null
        } else {
            cachedValue.value
        }
    }

    protected fun invalidate(key: String) {
        cache.invalidate(key)
    }

    protected fun putInCache(key: String, cachedValue: CachedValue) {
        cache.put(key, cachedValue)
    }

    @JvmOverloads
    fun getBaseAccessToken(retries: Int = 1): String {
        getAppId()
        val cachedAccessToken = getFromCacheIfPresent(BASE_ACCESS_TOKEN_KEY)
        return if (cachedAccessToken == null) {
            val accessToken = getForObject<BasicAccessToken>(
                    properties.basicAccessTokenUrl,
                    getAppId(),
                    getSecret()
            )
            if (accessToken.isOk) {
                put(
                        BASE_ACCESS_TOKEN_KEY,
                        CachedValue(
                                accessToken.accessToken!!,
                                LocalDateTime.now().plusSeconds(accessToken.expiresIn!!.toLong())
                        )
                )
                accessToken.accessToken
            } else if (retries < properties.maxRetries && accessToken.errcode != 40164) {
                getBaseAccessToken(retries + 1)
            } else {
                throw RuntimeException("获取access_token失败：errcode:${accessToken.errcode},errmsg:${accessToken.errmsg}")
            }
        } else {
            cachedAccessToken
        }
    }

    protected fun getSecret(): String = properties.secret

    protected fun getAppId(): String {
        val appId = properties.appId
        if (lastAppId != appId) {
            cache.invalidateAll()
            lastAppId = appId
        }

        return appId
    }
}