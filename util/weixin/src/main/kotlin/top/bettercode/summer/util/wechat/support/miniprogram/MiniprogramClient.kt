package top.bettercode.summer.util.wechat.support.miniprogram

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.getForObject
import top.bettercode.simpleframework.support.client.ApiTemplate
import top.bettercode.summer.util.wechat.config.MiniprogramProperties
import top.bettercode.summer.util.wechat.support.miniprogram.entity.JsSession

/**
 *
 * @author Peter Wu
 */
class MiniprogramClient(val properties: MiniprogramProperties) :
    ApiTemplate(
        "第三方接口",
        "微信小程序",
        "wexin-miniprogram",
        properties.connectTimeout,
        properties.readTimeout
    ) {

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
    }

    fun code2Session(code: String): JsSession {
        return getForObject(
            "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={1}&grant_type=authorization_code",
            properties.appId,
            properties.secret,
            code
        )
    }

}