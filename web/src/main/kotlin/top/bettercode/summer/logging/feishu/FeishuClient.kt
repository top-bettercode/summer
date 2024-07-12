package top.bettercode.summer.logging.feishu

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.type.TypeFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
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
import top.bettercode.summer.tools.lang.log.AlarmAppender.Companion.NO_ALARM_LOG_MARKER
import top.bettercode.summer.tools.lang.util.TimeUtil
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
        headers.contentType = MediaType(MediaType.APPLICATION_JSON, Charsets.UTF_8)
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
        responseType: Type,
        request: Any? = null,
        method: HttpMethod = HttpMethod.POST,
        contentType: MediaType? = null,
        requestToken: Boolean = false
    ): T? {
        val headers = HttpHeaders()
        headers.setBearerAuth(getToken(requestToken))
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

    /**
     * https://open.feishu.cn/document/server-docs/group/chat/list
     */
    fun chatList(): List<FeishuChat>? {
        val type = TypeFactory.defaultInstance()
            .constructParametricType(FeishuPageData::class.java, FeishuChat::class.java)
        val resultType = TypeFactory.defaultInstance()
            .constructParametricType(FeishuDataResult::class.java, type)

        val result: FeishuDataResult<FeishuPageData<FeishuChat>>? =
            request(url = "/im/v1/chats", responseType = resultType, method = HttpMethod.GET)

        if (result?.isOk() != true) {
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "feishu api request fail:{}",
                result?.msg
            )
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
        logUrl: String,
        linkTitle: String
    ): Boolean {
        val titles = title.split(Regex(" +"))
        val mainTitle=titles.first()
        val tag1=titles.getOrElse(1) { "" }
        val tag2=titles.getOrElse(2) { "" }
        val params =
            mapOf(
                "receive_id" to chatId,
                "msg_type" to "interactive",
                "content" to """{"header":{"template":"yellow","title":{"content":"$mainTitle","tag":"plain_text"},"subtitle":{"tag":"plain_text","content":"$subTitle"},"text_tag_list":[{"tag":"text_tag","text":{"tag":"plain_text","content":"$tag1"},"color":"turquoise"},{"tag":"text_tag","text":{"tag":"plain_text","content":"$tag2"},"color":"green"}]},"card_link": {"url": "$logUrl/$linkTitle"},"elements":[{"tag":"div","text":{"content":"$initialComment","tag":"plain_text"}}]}""".trimIndent(),
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
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "feishu api request fail:{}",
                result?.msg
            )
        }
        return result?.isOk() == true
    }

    fun filesUpload(
        chatId: String,
        timeStamp: Long,
        title: String,
        message: List<String>
    ): Boolean {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file_type", "stream")
        body.add("file_name", "$title-${TimeUtil.format(timeStamp)}.txt")
        val resource = ByteArrayResource(message.joinToString("\n").toByteArray())
        body.add("file", resource)

        val type = TypeFactory.defaultInstance()
            .constructParametricType(FeishuDataResult::class.java, FeishuFile::class.java)

        //https://open.feishu.cn/document/server-docs/im-v1/file/create
        val fileResult: FeishuDataResult<FeishuFile>? = request(
            url = "/im/v1/files",
            responseType = type,
            request = body,
            contentType = MediaType.MULTIPART_FORM_DATA
        )
        if (log.isTraceEnabled) {
            log.trace("feishu result:{}", fileResult)
        }
        if (fileResult?.isOk() != true) {
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "feishu api request fail:{}",
                fileResult?.msg
            )
            return false
        }
        val fileKey = fileResult.data?.fileKey
        if (fileKey == null) {
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "feishu api request fail:{}",
                fileResult.msg
            )
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
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "feishu api request fail:{}",
                result?.msg
            )
        }
        return result?.isOk() == true
    }

}
