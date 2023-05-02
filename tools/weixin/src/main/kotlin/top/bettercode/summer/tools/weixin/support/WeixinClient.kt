package top.bettercode.summer.tools.weixin.support

import com.fasterxml.jackson.annotation.JsonInclude
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.lang.Nullable
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

    protected val cache =
            Caffeine.newBuilder().expireAfterWrite(properties.cacheSeconds, TimeUnit.SECONDS)
                    .maximumSize(1000).build<String, CachedValue>()


    companion object {
        const val baseAccessTokenKey: String = "access_token"
    }

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
                object : MappingJackson2HttpMessageConverter() {
                    override fun canRead(mediaType: MediaType?): Boolean {
                        return true
                    }

                    override fun canWrite(clazz: Class<*>, @Nullable mediaType: MediaType?): Boolean {
                        return true
                    }
                }
        val objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(messageConverter)
        super.setMessageConverters(messageConverters)
    }

    @JvmOverloads
    fun getBaseAccessToken(retries: Int = 1): String {
        getAppId()
        val cachedValue = cache.getIfPresent(baseAccessTokenKey)
        return if (cachedValue == null || cachedValue.expired) {
            val accessToken = getForObject<BasicAccessToken>(
                    properties.basicAccessTokenUrl,
                    getAppId(),
                    getSecret()
            )
            if (accessToken.isOk) {
                cache.put(
                        baseAccessTokenKey,
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
            cachedValue.value
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