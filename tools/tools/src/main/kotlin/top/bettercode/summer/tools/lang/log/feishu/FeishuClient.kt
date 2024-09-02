package top.bettercode.summer.tools.lang.log.feishu

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import top.bettercode.summer.tools.lang.ExpiringValue
import top.bettercode.summer.tools.lang.log.AlarmAppender
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
import java.lang.reflect.Type
import java.time.Duration


/**
 *
 * https://open.feishu.cn/document/server-docs/api-call-guide/calling-process/overview
 * @author Peter Wu
 */
class FeishuClient(
    private val appId: String,
    private val appSecret: String
) {
    private val api = "https://open.feishu.cn/open-apis"
    private val log: Logger = LoggerFactory.getLogger(FeishuClient::class.java)
    private val restTemplate: RestTemplate = RestTemplate()
    private var token: ExpiringValue<String>? = null


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
    }

    /**
     * https://open.feishu.cn/document/server-docs/authentication-management/access-token/app_access_token_internal
     */
    private fun requestToken(): ExpiringValue<String> {
        val headers = HttpHeaders()
        headers.contentType =
            MediaType(MediaType.APPLICATION_JSON, mapOf("charset" to Charsets.UTF_8.name()))
        val requestEntity = HttpEntity(mapOf("app_id" to appId, "app_secret" to appSecret), headers)
        val authToken: FeishuTokenResult = restTemplate.postForObject(
            "$api/auth/v3/tenant_access_token/internal",
            requestEntity,
            FeishuTokenResult::class
        )
        return if (authToken.isOk()) {
            ExpiringValue(
                authToken.tenantAccessToken!!,
                Duration.ofSeconds(authToken.expire!!.toLong())
            )
        } else {
            throw RuntimeException("获取飞书token失败:${authToken.msg}")
        }
    }

    private fun getToken(requestToken: Boolean): String {
        synchronized(this) {
            if (requestToken) {
                token = requestToken()
            }
            var value = token?.value
            if (value == null) {
                token = requestToken()
                value = token?.value
            }
            return value ?: throw RuntimeException("获取飞书token失败")
        }
    }

    private fun <T : FeishuResult> request(
        url: String,
        request: Any? = null,
        method: HttpMethod = HttpMethod.POST,
        contentType: MediaType? = null,
        requestToken: Boolean = false,
        responseType: Type,
    ): T? {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer ${getToken(requestToken)}")
        if (contentType != null) {
            headers.contentType = contentType
        }
        val requestCallback = restTemplate.httpEntityCallback<T>(
            HttpEntity(request, headers), responseType
        )
        val responseEntityExtractor =
            restTemplate.responseEntityExtractor<T>(responseType)

        val response =
            restTemplate.execute(api + url, method, requestCallback, responseEntityExtractor)
        val body = response?.body
        if (body != null) {
            if (body.isInvalidAccessToken()) {
                return request(
                    url = url,
                    responseType = responseType,
                    request = request,
                    method = method,
                    contentType = contentType,
                    requestToken = true
                )
            }
        }
        return body
    }

    private val feishuChatType = object :
        ParameterizedTypeReference<FeishuDataResult<FeishuPageData<FeishuChat>>>() {}.type

    /**
     * https://open.feishu.cn/document/server-docs/group/chat/list
     */
    fun chatList(): List<FeishuChat>? {
        val result: FeishuDataResult<FeishuPageData<FeishuChat>>? =
            request(
                url = "/im/v1/chats",
                responseType = feishuChatType,
                method = HttpMethod.GET
            )

        if (result?.isOk() != true) {
            log.error(AlarmAppender.NO_ALARM_MARKER, "feishu api request fail:{}", result?.msg)
        }
        val chats = result?.data?.items
        if (log.isDebugEnabled) {
            log.debug("feishu channels:{}", chats)
        }
        return chats
    }

    /**
     * @param channel channel id or channel name
     * @return true or false
     */
    fun chatExist(channel: String): Boolean {
        return chatList()?.find { it.name == channel || it.chatId == channel } != null
    }

    fun chatIdByName(channelName: String): String? {
        return chatList()?.find { it.name == channelName }?.chatId
    }

    /**
     * https://open.feishu.cn/document/server-docs/im-v1/message/create
     */
    fun postMessage(
        chatId: String,
        title: String,
        subTitle: String,
        initialComment: String,
        template: Array<String>,
        linkTitle: String,
        message: String? = null,
        logUrl: String? = null,
    ): Boolean {
        val params =
            mapOf(
                "receive_id" to chatId,
                "msg_type" to "interactive",
                "content" to StringUtil.json(
                    params(
                        title = title,
                        subTitle = subTitle,
                        initialComment = initialComment,
                        template = template,
                        linkTitle = linkTitle,
                        message = message,
                        logUrl = logUrl,
                    )
                ),
            )

        if (log.isTraceEnabled) {
            log.trace("feishu params:{}", params)
        }

        val result: FeishuResult? = request(
            url = "/im/v1/messages?receive_id_type=chat_id",
            responseType = FeishuResult::class.java,
            request = params
        )
        if (log.isTraceEnabled) {
            log.trace("feishu result:{}", result)
        }
        if (result?.isOk() != true) {
            log.error(AlarmAppender.NO_ALARM_MARKER, "feishu api request fail:{}", result?.msg)
        }
        return result?.isOk() == true
    }

    private val feishuFileType = object :
        ParameterizedTypeReference<FeishuDataResult<FeishuFile>>() {}.type

    fun filesUpload(
        chatId: String,
        timeStamp: Long,
        title: String,
        message: List<String>
    ): Boolean {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file_type", "stream")
        body.add(
            "file_name",
            "$title-${TimeUtil.format(timeStamp, DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN)}.txt"
        )
        val resource = ByteArrayResource(message.joinToString("\n").toByteArray())
        body.add("file", resource)

        //https://open.feishu.cn/document/server-docs/im-v1/file/create
        val fileResult: FeishuDataResult<FeishuFile>? = request(
            url = "/im/v1/files",
            responseType = feishuFileType,
            request = body,
            contentType = MediaType.MULTIPART_FORM_DATA
        )
        if (log.isTraceEnabled) {
            log.trace("feishu result:{}", fileResult)
        }
        if (fileResult?.isOk() != true) {
            log.error(AlarmAppender.NO_ALARM_MARKER, "feishu api request fail:{}", fileResult?.msg)
            return false
        }
        val fileKey = fileResult.data?.fileKey
        if (fileKey == null) {
            log.error(AlarmAppender.NO_ALARM_MARKER, "feishu api request fail:{}", fileResult.msg)
            return false
        }
        val params = mapOf(
            "receive_id" to chatId,
            "msg_type" to "file",
            "content" to "{\"file_key\":\"$fileKey\"}"
        )
        val result: FeishuResult? = request(
            url = "/im/v1/messages?receive_id_type=chat_id",
            responseType = FeishuResult::class.java,
            request = params
        )

        if (log.isTraceEnabled) {
            log.trace("feishu result:{}", result)
        }
        if (result?.isOk() != true) {
            log.error(AlarmAppender.NO_ALARM_MARKER, "feishu api request fail:{}", result?.msg)
        }
        return result?.isOk() == true
    }

    companion object {

        /**
         * https://open.feishu.cn/document/uAjLw4CM/ukzMukzMukzM/feishu-cards/card-components/content-components/title
         */
        fun template(level: Level): Array<String> {
            when {
                level.levelInt >= Level.ERROR_INT -> {
                    return arrayOf(
                        "red",
                        "orange",
                        "carmine",
                        "neutral",
                        "more-close_outlined",
                        "red"
                    )
                }

                level == Level.WARN -> {
                    return arrayOf(
                        "orange",
                        "yellow",
                        "red",
                        "neutral",
                        "warning_outlined",
                        "orange"
                    )
                }

                else -> {
                    return arrayOf(
                        "green",
                        "lime",
                        "turquoise",
                        "neutral",
                        "info_outlined",
                        "green"
                    )
                }
            }

        }

        fun params(
            title: String,
            subTitle: String,
            initialComment: String,
            template: Array<String>,
            linkTitle: String,
            message: String?,
            logUrl: String?,
        ): Map<String, Any> {
            val titles = title.split(Regex(" +"))
            val mainTitle = titles.first()
            val tag1 = titles.getOrElse(1) { "" }
            val tag2 = titles.getOrElse(2) { "" }
            val tag3 = if (linkTitle.contains("#")) linkTitle.substringAfter("#") else ""
            val params = mapOf(
                "config" to mapOf(
                    "width_mode" to "fill"
                ),
                "header" to mapOf(
                    "template" to template[0],
                    "title" to mapOf(
                        "content" to mainTitle,
                        "tag" to "plain_text"
                    ),
                    "subtitle" to mapOf(
                        "tag" to "plain_text",
                        "content" to subTitle
                    ),
                    "text_tag_list" to if (tag3.isBlank()) listOf(
                        mapOf(
                            "tag" to "text_tag",
                            "text" to mapOf(
                                "tag" to "plain_text",
                                "content" to tag1
                            ),
                            "color" to template[1]
                        ),
                        mapOf(
                            "tag" to "text_tag",
                            "text" to mapOf(
                                "tag" to "plain_text",
                                "content" to tag2
                            ),
                            "color" to template[2]
                        )
                    ) else listOf(
                        mapOf(
                            "tag" to "text_tag",
                            "text" to mapOf(
                                "tag" to "plain_text",
                                "content" to tag1
                            ),
                            "color" to template[1]
                        ),
                        mapOf(
                            "tag" to "text_tag",
                            "text" to mapOf(
                                "tag" to "plain_text",
                                "content" to tag2
                            ),
                            "color" to template[2]
                        ),
                        mapOf(
                            "tag" to "text_tag",
                            "text" to mapOf(
                                "tag" to "plain_text",
                                "content" to tag3
                            ),
                            "color" to template[3]
                        )
                    ),
                    "ud_icon" to mapOf(
                        "token" to template[4],
                        "style" to mapOf(
                            "color" to template[5]
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
                            "tag" to "plain_text",
                        )
                    )
                )
            )
            return params
        }

    }
}
