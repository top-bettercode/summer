package top.bettercode.summer.tools.weixin.support

import com.fasterxml.jackson.annotation.JsonInclude
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.tools.autodoc.AutodocUtil.objectMapper
import top.bettercode.summer.tools.weixin.properties.IWeixinProperties
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.ApiQuota
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.BasicAccessToken
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.CachedValue
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.StableTokenRequest
import top.bettercode.summer.web.support.client.ApiTemplate
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * 公众号接口
 *
 *
 * @author Peter Wu
 */
open class WeixinClient<T : IWeixinProperties>(
        val properties: T,
        collectionName: String,
        name: String,
        logMarker: String
) : ApiTemplate(
        collectionName,
        name,
        logMarker,
        properties.connectTimeout,
        properties.readTimeout
) {

    private var lastAppId = properties.appId

    companion object {
        const val BASE_ACCESS_TOKEN_KEY: String = "access_token"
        const val STABLE_ACCESS_TOKEN_KEY: String = "stable_access_token"
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

    fun writeToXml(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    private val cache: Cache<String, CachedValue> by lazy {
        Caffeine.newBuilder().expireAfterWrite(properties.cacheSeconds, TimeUnit.SECONDS)
                .maximumSize(1000).build()
    }


    protected open fun putCache(key: String, value: CachedValue) {
        cache.put(key, value)
    }

    protected open fun getCache(key: String): CachedValue? {
        return cache.getIfPresent(key)
    }

    protected open fun clearCache() {
        cache.invalidateAll()
    }

    @Synchronized
    protected fun putIfAbsent(key: String, callable: Callable<CachedValue>): String {
        val cachedValue = getCache(key)
        return if (cachedValue == null || cachedValue.expired) {
            val value = callable.call()
            putCache(key, value)
            value.value
        } else {
            cachedValue.value
        }
    }

    @Deprecated("多环境下会造成频繁失效，请使用getStableAccessToken()")
    @JvmOverloads
    @Synchronized
    fun getBaseAccessToken(retries: Int = 1): String {
        return putIfAbsent(BASE_ACCESS_TOKEN_KEY + ":" + properties.appId) {
            getToken(retries)
        }
    }

    /**
     *https://developers.weixin.qq.com/doc/offiaccount/openApi/get_api_quota.html
     */
    fun getApiQuota(cgiPath: String): ApiQuota {
        return postForObject<ApiQuota>("https://api.weixin.qq.com/cgi-bin/openapi/quota/get?access_token=${getStableAccessToken()}", mapOf("cgi_path" to cgiPath))
    }

    /**
     * 使用AppSecret重置 API 调用次数
     * https://developers.weixin.qq.com/doc/offiaccount/openApi/clearQuotaByAppSecret.html
     */
    fun clearQuotaByAppSecret(): WeixinResponse {
        return postForObject<WeixinResponse>("https://api.weixin.qq.com/cgi-bin/clear_quota/v2?appid=${properties.appId}&appsecret=${properties.secret}")
    }

    private fun getToken(retries: Int = 1): CachedValue {
        val accessToken = getForObject<BasicAccessToken>(
                properties.basicAccessTokenUrl,
                properties.appId,
                properties.secret
        )
        return if (accessToken.isOk) {
            CachedValue(
                    accessToken.accessToken!!,
                    Duration.ofSeconds(accessToken.expiresIn!!.toLong())
            )
        } else if (retries < properties.maxRetries && accessToken.errcode != 40164) {
            //40164 调用接口的IP地址不在白名单中，请在接口IP白名单中进行设置。
            getToken(retries + 1)
        } else {
            throw RuntimeException("获取access_token失败：errcode:${accessToken.errcode},errmsg:${accessToken.errmsg}")
        }
    }

    @JvmOverloads
    @Synchronized
    fun getStableAccessToken(retries: Int = 1): String {
        return putIfAbsent(STABLE_ACCESS_TOKEN_KEY + ":" + properties.appId) {
            getStableToken(false, retries)
        }
    }

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getStableAccessToken.html
     */
    private fun getStableToken(forceRefresh: Boolean, retries: Int = 1): CachedValue {
        val accessToken = postForObject<BasicAccessToken>(
                "https://api.weixin.qq.com/cgi-bin/stable_token",
                StableTokenRequest(
                        properties.appId,
                        properties.secret,
                        forceRefresh)
        )
        return if (accessToken.isOk) {
            CachedValue(
                    accessToken.accessToken!!,
                    Duration.ofSeconds(accessToken.expiresIn!!.toLong())
            )
        } else if (retries < properties.maxRetries && accessToken.errcode != 40164 && accessToken.errcode != 45009) {
            //40164 调用接口的IP地址不在白名单中，请在接口IP白名单中进行设置。
            //45009 reach max api daily quota limit rid: 6566e923-2f119a17-68648caf
            getStableToken(forceRefresh = forceRefresh, retries = retries + 1)
        } else {
            throw RuntimeException("获取access_token失败：errcode:${accessToken.errcode},errmsg:${accessToken.errmsg}")
        }
    }
}