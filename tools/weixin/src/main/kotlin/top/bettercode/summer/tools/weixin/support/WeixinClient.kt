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
import java.util.concurrent.Callable
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


    protected open fun putCache(key: String, value: CachedValue) {
        cache.put(key, value)
    }

    protected open fun putCacheIfAbsent(key: String, callable: Callable<CachedValue>): CachedValue? {
        return cache.get(key) {
            callable.call()
        }
    }

    @JvmOverloads
    protected open fun invalidateCache(key: String? = null) {
        if (key == null) {
            cache.invalidateAll()
        } else
            cache.invalidate(key)
    }

    @Synchronized
    protected fun putIfAbsent(key: String, callable: Callable<CachedValue>): String {
        val cachedValue = putCacheIfAbsent(key, callable)
        return if (cachedValue == null || cachedValue.expired) {
            val value = callable.call()
            putCache(key, value)
            value.value
        } else {
            cachedValue.value
        }
    }

    @JvmOverloads
    fun getBaseAccessToken(retries: Int = 1): String {
        val appId = getAppId()
        return putIfAbsent(BASE_ACCESS_TOKEN_KEY) {
            getToken(appId, retries)
        }
    }

    private fun getToken(appId: String, retries: Int = 1): CachedValue {
        val accessToken = getForObject<BasicAccessToken>(
                properties.basicAccessTokenUrl,
                appId,
                getSecret()
        )
        return if (accessToken.isOk) {
            CachedValue(
                    accessToken.accessToken!!,
                    LocalDateTime.now().plusSeconds(accessToken.expiresIn!!.toLong())
            )
        } else if (retries < properties.maxRetries && accessToken.errcode != 40164) {
            //40164 调用接口的IP地址不在白名单中，请在接口IP白名单中进行设置。
            getToken(appId, retries + 1)
        } else {
            throw RuntimeException("获取access_token失败：errcode:${accessToken.errcode},errmsg:${accessToken.errmsg}")
        }
    }

    protected fun getSecret(): String = properties.secret

    protected fun getAppId(): String {
        val appId = properties.appId
        if (lastAppId != appId) {
            invalidateCache()
            lastAppId = appId
        }

        return appId
    }
}