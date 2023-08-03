package top.bettercode.summer.tools.rapidauth

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.lang.Nullable
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.RandomUtil
import top.bettercode.summer.tools.lang.util.Sha256DigestUtils
import top.bettercode.summer.tools.rapidauth.RapidauthClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.rapidauth.entity.RapidauthRequest
import top.bettercode.summer.tools.rapidauth.entity.RapidauthResponse
import top.bettercode.summer.web.support.client.ApiTemplate

/**
 * 手机号查询接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class RapidauthClient(
        private val properties: RapidauthProperties
) : ApiTemplate(
        "第三方平台",
        "腾讯云号码认证",
        LOG_MARKER,
        properties.connectTimeout,
        properties.readTimeout
), IRapidauthClient {

    companion object {
        const val LOG_MARKER = "rapidauth"
    }

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
                object : MappingJackson2HttpMessageConverter() {
                    override fun canRead(@Nullable mediaType: MediaType?): Boolean {
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


    override fun query(carrier: String, token: String): RapidauthResponse {
        val appkey = properties.appkey
        val random = RandomUtil.nextIntString(10) //随机数字串，生成10位
        val time = (System.currentTimeMillis() / 1000).toString()

        val sig: String = Sha256DigestUtils.shaHex("appkey=$appkey&random=$random&time=$time")

        val request = RapidauthRequest(sig, time, carrier, token)


        val requestCallback = httpEntityCallback<Any>(
                HttpEntity(request),
                RapidauthResponse::class.java
        )
        val entity: ResponseEntity<RapidauthResponse> = try {
            execute(
                    properties.url, HttpMethod.POST,
                    requestCallback,
                    responseEntityExtractor<RapidauthResponse>(RapidauthResponse::class.java),
                    properties.sdkappid,
                    random
            )
        } catch (e: Exception) {
            throw RapidauthException(e)
        } ?: throw RapidauthException()

        return if (entity.statusCode.is2xxSuccessful) {
            val body = entity.body
            if (body?.isOk() == true) {
                body
            } else {
                val message = body?.errmsg
                throw RapidauthSysException(message ?: "请求失败")
            }
        } else {
            throw RapidauthException()
        }
    }

}