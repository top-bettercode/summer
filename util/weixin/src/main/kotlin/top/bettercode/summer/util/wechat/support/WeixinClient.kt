package top.bettercode.summer.util.wechat.support

import com.fasterxml.jackson.annotation.JsonInclude
import com.google.common.cache.CacheBuilder
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.web.client.getForObject
import top.bettercode.simpleframework.support.client.ApiTemplate
import top.bettercode.summer.util.wechat.config.IWexinProperties
import top.bettercode.summer.util.wechat.config.WexinProperties
import top.bettercode.summer.util.wechat.support.offiaccount.entity.BasicAccessToken
import top.bettercode.summer.util.wechat.support.offiaccount.entity.CachedValue
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

    protected val cache =
        CacheBuilder.newBuilder().expireAfterWrite(properties.cacheSeconds, TimeUnit.SECONDS)
            .maximumSize(1000).build<String, CachedValue>()


    companion object {
        const val maxRetries = 2
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
        val cachedValue = cache.getIfPresent(baseAccessTokenKey)
        return if (cachedValue == null || cachedValue.expired) {
            val accessToken = getForObject<BasicAccessToken>(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}",
                properties.appId,
                properties.secret
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
            } else if (retries < maxRetries) {
                getBaseAccessToken(retries + 1)
            } else {
                throw RuntimeException("获取access_token失败：errcode:${accessToken.errcode},errmsg:${accessToken.errmsg}")
            }
        } else {
            cachedValue.value
        }
    }

}