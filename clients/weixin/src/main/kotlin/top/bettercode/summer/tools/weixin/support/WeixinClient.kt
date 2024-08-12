package top.bettercode.summer.tools.weixin.support

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import top.bettercode.summer.tools.lang.ExpiringValue
import top.bettercode.summer.tools.lang.client.ApiTemplate
import top.bettercode.summer.tools.weixin.properties.WeixinProperties
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.ApiQuota
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.BasicAccessToken
import top.bettercode.summer.tools.weixin.support.offiaccount.entity.StableTokenRequest
import java.time.Duration
import java.util.concurrent.Callable

/**
 * 公众号接口
 *
 *
 * @author Peter Wu
 */
open class WeixinClient<T : WeixinProperties>(
    properties: T,
    val cache: IWeixinCache,
    marker: String
) : ApiTemplate<T>(
    marker = marker,
    properties = properties
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
        this.messageConverters = messageConverters
    }

    protected open fun clearBaseTokenCache() {
        cache.evict(BASE_ACCESS_TOKEN_KEY + ":" + properties.appId)
    }

    protected open fun clearStableTokenCache() {
        cache.evict(STABLE_ACCESS_TOKEN_KEY + ":" + properties.appId)
    }

    protected fun putIfAbsent(key: String, callable: Callable<ExpiringValue<String>>): String {
        synchronized(this) {
            val cachedValue = cache.get(key)
            val cacheValue = cachedValue?.value
            return if (cacheValue == null) {
                val value = callable.call()
                cache.put(key, value)
                value.originalValue
            } else {
                cacheValue
            }
        }
    }

    /**
     * 获取接口调用凭据
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getAccessToken.html
     */
    @Deprecated("多环境下会造成频繁失效，请使用getStableAccessToken()")
    @JvmOverloads
    open fun getBaseAccessToken(retries: Int = 1): String {
        return putIfAbsent(BASE_ACCESS_TOKEN_KEY + ":" + properties.appId) {
            getToken(retries)
        }
    }

    /**
     *https://developers.weixin.qq.com/doc/offiaccount/openApi/get_api_quota.html
     */
    open fun getApiQuota(cgiPath: String): ApiQuota {
        return postForObject<ApiQuota>(
            "https://api.weixin.qq.com/cgi-bin/openapi/quota/get?access_token=${getStableAccessToken()}",
            mapOf("cgi_path" to cgiPath)
        )
    }

    /**
     * 使用AppSecret重置 API 调用次数
     * https://developers.weixin.qq.com/doc/offiaccount/openApi/clearQuotaByAppSecret.html
     */
    open fun clearQuotaByAppSecret(): WeixinResponse {
        return postForObject<WeixinResponse>("https://api.weixin.qq.com/cgi-bin/clear_quota/v2?appid=${properties.appId}&appsecret=${properties.secret}")
    }

    private fun getToken(retries: Int = 1): ExpiringValue<String> {
        val accessToken = getForObject<BasicAccessToken>(
            properties.basicAccessTokenUrl,
            properties.appId,
            properties.secret
        )
        return if (accessToken.isOk) {
            ExpiringValue(
                accessToken.accessToken!!,
                Duration.ofSeconds(accessToken.expiresIn!!.toLong())
            )
        } else if (retries < properties.maxRetries && accessToken.errcode != 40164) {
            //40164 调用接口的IP地址不在白名单中，请在接口IP白名单中进行设置。
            getToken(retries + 1)
        } else {
            throw clientException("获取access_token失败：errcode:${accessToken.errcode},errmsg:${accessToken.errmsg}")
        }
    }

    @JvmOverloads
    open fun getStableAccessToken(retries: Int = 1): String {
        return putIfAbsent(STABLE_ACCESS_TOKEN_KEY + ":" + properties.appId) {
            getStableToken(false, retries)
        }
    }

    /**
     * https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getStableAccessToken.html
     */
    private fun getStableToken(forceRefresh: Boolean, retries: Int = 1): ExpiringValue<String> {
        val accessToken = postForObject<BasicAccessToken>(
            "https://api.weixin.qq.com/cgi-bin/stable_token",
            StableTokenRequest(
                properties.appId,
                properties.secret,
                forceRefresh
            )
        )
        return if (accessToken.isOk) {
            ExpiringValue(
                accessToken.accessToken!!,
                Duration.ofSeconds(accessToken.expiresIn!!.toLong())
            )
        } else if (retries < properties.maxRetries && accessToken.errcode != 40164 && accessToken.errcode != 45009 && accessToken.errcode != 45011) {
            //40164 调用接口的IP地址不在白名单中，请在接口IP白名单中进行设置。
            //45009 reach max api daily quota limit 	调用超过天级别频率限制。可调用clear_quota接口恢复调用额度。
            //45011 api minute-quota reach limit  mustslower  retry next minute 	API 调用太频繁，请稍候再试
            getStableToken(forceRefresh = forceRefresh, retries = retries + 1)
        } else {
            throw clientException("获取access_token失败：errcode:${accessToken.errcode},errmsg:${accessToken.errmsg}")
        }
    }
}