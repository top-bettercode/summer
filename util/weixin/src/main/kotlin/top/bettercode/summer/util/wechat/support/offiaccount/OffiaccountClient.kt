package top.bettercode.summer.util.wechat.support.offiaccount

import com.fasterxml.jackson.annotation.JsonInclude
import com.google.common.cache.CacheBuilder
import org.slf4j.MarkerFactory
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.getForObject
import top.bettercode.lang.util.Sha1DigestUtil
import top.bettercode.simpleframework.support.client.ApiTemplate
import top.bettercode.summer.util.wechat.config.OffiaccountProperties
import top.bettercode.summer.util.wechat.support.offiaccount.entity.*
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
class OffiaccountClient(val properties: OffiaccountProperties) :
    ApiTemplate("第三方接口", "微信公众号", "wexin-offiaccount", properties.connectTimeout, properties.readTimeout) {

    private val cache =
        CacheBuilder.newBuilder().expireAfterWrite(properties.cacheSeconds, TimeUnit.SECONDS)
            .maximumSize(1000).build<String, CachedValue>()

    private val maxRetries = 3

    companion object {
        const val baseAccessTokenKey: String = "access_token"
        const val jsapiTicketKey: String = "jsapi_ticket"
    }

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
            object : MappingJackson2HttpMessageConverter() {
                override fun canRead(mediaType: MediaType?): Boolean {
                    return true
                }

                override fun canWrite(clazz: Class<*>?, mediaType: MediaType?): Boolean {
                    return true
                }
            }
        val objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(messageConverter)
        setMessageConverters(messageConverters)

        try {
            val url =
                "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s#wechat_redirect"
            val authenticationUrl = String.format(
                url,
                properties.appId,
                URLEncoder.encode(properties.oauthUrl, "UTF-8"),
                "snsapi_userinfo"
            )
            log.info(MarkerFactory.getMarker(logMarker), "authenticationUrl:{}", authenticationUrl)
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }

    fun getBaseAccessToken(retries: Int = 0): String {
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

    fun getJsapiTicket(retries: Int = 0): String {
        val cachedValue = cache.getIfPresent(jsapiTicketKey)
        return if (cachedValue == null || cachedValue.expired) {
            val jsapiTicket = getForObject<JsapiTicket>(
                "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi",
                getBaseAccessToken()
            )
            if (jsapiTicket.isOk) {
                cache.put(
                    jsapiTicketKey,
                    CachedValue(
                        jsapiTicket.ticket!!,
                        LocalDateTime.now().plusSeconds(jsapiTicket.expiresIn!!.toLong())
                    )
                )
                jsapiTicket.ticket
            } else if (retries < maxRetries) {
                getJsapiTicket(retries + 1)
            } else {
                throw RuntimeException("获取jsapiTicket失败：errcode:${jsapiTicket.errcode},errmsg:${jsapiTicket.errmsg}")
            }
        } else {
            cachedValue.value
        }
    }

    fun getWebPageAccessToken(code: String): WebPageAccessToken {
        return getForObject(
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code",
            properties.appId,
            properties.secret,
            code
        )
    }

    //--------------------------------------------

    fun jsSignUrl(url: String): JsapiSignature {
        val nonceStr = UUID.randomUUID().toString()
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val jsapiTicket = getJsapiTicket()
        //注意这里参数名必须全部小写，且必须有序
        val signature = Sha1DigestUtil.shaHex(
            "jsapi_ticket=" + jsapiTicket +
                    "&noncestr=" + nonceStr +
                    "&timestamp=" + timestamp +
                    "&url=" + url
        )

        return JsapiSignature(signature, properties.appId, nonceStr, timestamp)
    }

    fun shaHex(vararg str: String): String {
        str.sort()
        return Sha1DigestUtil.shaHex(str.joinToString(""))
    }

}