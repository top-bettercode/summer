package top.bettercode.summer.logging.feishu

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.type.TypeFactory
import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import top.bettercode.summer.tools.lang.PrettyMessageHTMLLayout
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


/**
 *
 * https://open.feishu.cn/document/server-docs/api-call-guide/calling-process/overview
 * @author Peter Wu
 */
class FeishuClient(
    private val appId: String,
    private val appSecret: String,
    private val logsPath: String,
    private val managementLogPath: String
) {
    private val api = "https://open.feishu.cn/open-apis"
    private val log: Logger = LoggerFactory.getLogger(FeishuClient::class.java)
    private val restTemplate: RestTemplate = RestTemplate()
    private val tokenCache = Caffeine.newBuilder()
        .expireAfterWrite(7200, TimeUnit.SECONDS)
        .maximumSize(1000).build<String, String>()


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
    private fun requestToken(): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType(MediaType.APPLICATION_JSON, Charsets.UTF_8)
        val requestEntity = HttpEntity(mapOf("app_id" to appId, "app_secret" to appSecret), headers)
        val authToken: FeishuTokenResult = restTemplate.postForObject(
            "$api/auth/v3/tenant_access_token/internal",
            requestEntity,
            FeishuTokenResult::class
        )
        return if (authToken.isOk()) {
            authToken.tenantAccessToken!!
        } else {
            throw RuntimeException("获取飞书token失败:${authToken.msg}")
        }
    }

    private fun getToken(requestToken: Boolean): String {
        if (requestToken) {
            tokenCache.invalidateAll()
        }
        return tokenCache.get("token") { requestToken() }
            ?: throw RuntimeException("获取飞书token失败")
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
            log.error("feishu api request fail:{}", result?.msg)
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
        timeStamp: Long,
        title: String,
        initialComment: String,
        message: List<String>
    ): Boolean {
        val params = mutableMapOf(
            "receive_id" to chatId,
            "msg_type" to "post"
        )

        val actuatorAddress = try {
            top.bettercode.summer.logging.LoggingUtil.actuatorAddress
        } catch (e: Exception) {
            null
        }
        if (actuatorAddress == null) {
            return filesUpload(chatId, timeStamp, title, message)
        } else {
            val anchor = PrettyMessageHTMLLayout.anchor(message.last())
            val path = File(logsPath)
            val namePattern = "all-${TimeUtil.now().format("yyyy-MM-dd")}-"
            val files =
                path.listFiles { file -> file.name.startsWith(namePattern) && file.extension == "gz" }
            files?.sortBy { -it.lastModified() }
            val existFilename = files?.firstOrNull()?.nameWithoutExtension

            val filename = "$namePattern${
                if (existFilename != null) {
                    existFilename.substringAfter(namePattern).toInt() + 1
                } else {
                    0
                }
            }"
            val linkTitle = "${filename}.gz#$anchor"
            val logUrl = actuatorAddress + managementLogPath

            params["content"] =
                "{\"zh_cn\":{\"title\":\"$title\",\"content\":[[{\"tag\":\"text\",\"text\":\"$initialComment\\n\"},{\"tag\":\"a\",\"href\":\"$logUrl/${linkTitle}\",\"text\":\"$linkTitle\"}]]}}"

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
            return result?.isOk() == true
        }
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
            log.error("feishu api request fail:{}", fileResult?.msg)
            return false
        }
        val fileKey = fileResult.data?.fileKey
        if (fileKey == null) {
            log.error("feishu api request fail:{}", fileResult.msg)
            return false
        }
        val params = mutableMapOf(
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
        return result?.isOk() == true
    }

}
