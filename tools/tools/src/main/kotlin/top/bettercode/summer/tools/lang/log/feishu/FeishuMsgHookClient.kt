package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import top.bettercode.summer.tools.lang.log.AlarmAppender
import top.bettercode.summer.tools.lang.log.feishu.FeishuMsgClient.Companion.params
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot#f62e72d5
 *
 * @author Peter Wu
 */
class FeishuMsgHookClient(
    val webhook: String,
    val secret: String?
) {
    private val api = "https://open.feishu.cn/open-apis/bot/v2/hook/"
    private val log: Logger = LoggerFactory.getLogger(FeishuMsgHookClient::class.java)
    private val restTemplate: RestTemplate = RestTemplate()

    init {
        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(2000)
        //Read timeout
        clientHttpRequestFactory.setReadTimeout(10000)
        restTemplate.requestFactory = clientHttpRequestFactory

        val messageConverter = MappingJackson2HttpMessageConverter()
        messageConverter.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(AllEncompassingFormHttpMessageConverter())
        messageConverters.add(messageConverter)
        restTemplate.messageConverters = messageConverters

        restTemplate.errorHandler = object : DefaultResponseErrorHandler() {
            override fun handleError(response: ClientHttpResponse) {}
        }
    }

    /**
     * https://open.feishu.cn/document/server-docs/im-v1/message/create
     */
    fun postMessage(
        title: String,
        subTitle: String,
        initialComment: String,
        template: Array<String>,
        linkTitle: String,
        message: String? = null,
        logUrl: String? = null,
    ): Boolean {
        val params = mutableMapOf(
            "msg_type" to "interactive",
            "card" to params(
                title = title,
                subTitle = subTitle,
                initialComment = initialComment,
                template = template,
                message = message,
                logUrl = logUrl,
                linkTitle = linkTitle,
            )
        )
        secret?.let {
            val timestamp = System.currentTimeMillis() / 1000
            params["timestamp"] = timestamp
            params["sign"] = genSign(timestamp)
        }
        if (log.isTraceEnabled) {
            log.trace("feishu params:{}", params)
        }
        val headers = HttpHeaders()
        headers.contentType = MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)

        val result: FeishuResult? =
            restTemplate.postForObject(
                api + webhook,
                HttpEntity(params, headers),
                FeishuResult::class.java
            )

        if (log.isTraceEnabled) {
            log.trace("feishu result:{}", result)
        }
        if (result?.isOk() != true) {
            log.error(AlarmAppender.NO_ALARM_MARKER, "feishu api request fail:{}", result?.msg)
        }
        return result?.isOk() == true
    }


    private fun genSign(timestamp: Long): String {
        //把timestamp+"\n"+密钥当做签名字符串
        val stringToSign = "$timestamp\n$secret"
        //使用HmacSHA256算法计算签名
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(stringToSign.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val signData = mac.doFinal(byteArrayOf())
        return String(Base64.getEncoder().encode(signData))
    }
}