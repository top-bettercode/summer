package top.bettercode.summer.logging.feishu

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
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
import top.bettercode.summer.tools.lang.log.AlarmAppender.Companion.NO_ALARM_LOG_MARKER
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot#f62e72d5
 *
 * @author Peter Wu
 */
class FeishuHookClient(
    val webhook: String,
    val secret: String?
) {
    private val api = "https://open.feishu.cn/open-apis/bot/v2/hook/"
    private val log: Logger = LoggerFactory.getLogger(FeishuHookClient::class.java)
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
        message: String? = null,
        logUrl: String? = null,
        linkTitle: String? = null
    ): Boolean {
        val titles = title.split(Regex(" +"))
        val mainTitle = titles.first()
        val tag1 = titles.getOrElse(1) { "" }
        val tag2 = titles.getOrElse(2) { "" }
        val params = mutableMapOf(
            "msg_type" to "interactive",
            "card" to mapOf(
                "header" to mapOf(
                    "template" to "yellow",
                    "title" to mapOf(
                        "content" to mainTitle,
                        "tag" to "plain_text"
                    ),
                    "subtitle" to mapOf(
                        "tag" to "plain_text",
                        "content" to subTitle
                    ),
                    "text_tag_list" to listOf(
                        mapOf(
                            "tag" to "text_tag",
                            "text" to mapOf(
                                "tag" to "plain_text",
                                "content" to tag1
                            ),
                            "color" to "turquoise"
                        ),
                        mapOf(
                            "tag" to "text_tag",
                            "text" to mapOf(
                                "tag" to "plain_text",
                                "content" to tag2
                            ),
                            "color" to "green"
                        )
                    )
                ),
                "card_link" to if (message == null) mapOf(
                    "url" to "$logUrl/$linkTitle"
                ) else mapOf(),
                "elements" to if (message == null) listOf(
                    mapOf(
                        "tag" to "div",
                        "text" to mapOf(
                            "content" to initialComment,
                            "tag" to "plain_text"
                        )
                    )
                ) else listOf(
                    mapOf(
                        "tag" to "div",
                        "text" to mapOf(
                            "content" to initialComment,
                            "tag" to "plain_text"
                        )
                    ),
                    mapOf(
                        "tag" to "div",
                        "text" to mapOf(
                            "content" to message,
                            "tag" to "plain_text"
                        )
                    )
                )
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
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "feishu api request fail:{}",
                result?.msg
            )
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