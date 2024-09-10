package top.bettercode.summer.tools.rapidauth

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.ApiTemplate
import top.bettercode.summer.tools.lang.util.RandomUtil
import top.bettercode.summer.tools.lang.util.Sha256DigestUtils
import top.bettercode.summer.tools.rapidauth.entity.RapidauthRequest
import top.bettercode.summer.tools.rapidauth.entity.RapidauthResponse

/**
 * 手机号查询接口
 *
 * @author Peter Wu
 */
@LogMarker(RapidauthClient.MARKER)
open class RapidauthClient(
    properties: RapidauthProperties
) : ApiTemplate<RapidauthProperties>(
    marker = MARKER,
    properties = properties
) {

    companion object {
        const val MARKER = "rapidauth"
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

    /**
     * @param carrier 运营商，移动：mobile， 联通：unicom，电信：telecom
     * @param token token 有效期为2分钟
     */
    @JvmOverloads
    open fun query(carrier: String? = null, token: String): RapidauthResponse {
        val appkey = properties.appkey
        val random = RandomUtil.nextIntString(10) //随机数字串，生成10位
        val time = (System.currentTimeMillis() / 1000).toString()

        val sig: String = Sha256DigestUtils.shaHex("appkey=$appkey&random=$random&time=$time")

        val request = RapidauthRequest(sig, time, carrier, token)


        val requestCallback = this.httpEntityCallback<RapidauthResponse>(
            HttpEntity(request),
            RapidauthResponse::class.java
        )
        val entity: ResponseEntity<RapidauthResponse> =
            execute(
                properties.url, HttpMethod.POST,
                requestCallback,
                this.responseEntityExtractor(RapidauthResponse::class.java),
                properties.sdkappid,
                random
            ) ?: throw clientException()

        return entity.body ?: throw clientException()
    }

}