package top.bettercode.summer.logging.slack

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import top.bettercode.summer.tools.lang.PrettyMessageHTMLLayout
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.io.File

/**
 *
 * @author Peter Wu
 */
class SlackClient(
    private val authToken: String
) {

    private val api = "https://slack.com/api/"
    private val log: Logger = LoggerFactory.getLogger(SlackClient::class.java)
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
    }

    private fun <T> request(
        url: String,
        responseType: Class<T>,
        request: Any? = null,
        method: HttpMethod = HttpMethod.POST
    ): T? {
        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer $authToken")
        val requestEntity = HttpEntity(request, headers)
        val response = restTemplate.exchange(api + url, method, requestEntity, responseType)
        return response.body
    }

    fun channelsList(): List<Channel>? {
        val headers = HttpHeaders()
        headers.setBearerAuth(authToken)
        val result = request(
            url = "conversations.list?types=public_channel,private_channel&exclude_archived=true",
            responseType = ChannelsResult::class.java,
            method = HttpMethod.GET
        )
        if (result?.ok != true) {
            log.error("slack api request fail:{}", result?.error)
        }
        val channels = result?.channels
        if (log.isDebugEnabled) {
            log.debug("slack channels:{}", channels)
        }
        return channels
    }

    /**
     * @param channel channel id or channel name
     * @return true or false
     */
    fun channelExist(channel: String): Boolean {
        return channelsList()?.find { it.name == channel || it.id == channel } != null
    }

    fun channelIdByName(channelName: String): String? {
        return channelsList()?.find { it.name == channelName }?.id
    }


    /**
     * @param channel channel id or channel name
     */
    fun postMessage(
        channel: String,
        timeStamp: Long,
        title: String,
        initialComment: String,
        message: List<String>,
    ): Boolean {
        return filesUpload(channel, timeStamp, title, initialComment, message)
    }

    fun filesUpload(
        channel: String,
        timeStamp: Long,
        title: String,
        initialComment: String,
        message: List<String>
    ): Boolean {
        val params = LinkedMultiValueMap<String, Any>()
        params.add("token", authToken)
        params.add("channels", channel)
        params.add("content", message.joinToString("").toByteArray())
        params.add("filename", "$title-${TimeUtil.format(timeStamp)}.log")
        params.add("filetype", "text")
        if (title.isNotBlank()) {
            params.add("title", "$title-${TimeUtil.format(timeStamp)}")
        }
        params.add("initial_comment", "$title:\n$initialComment")
        val result = request(
            url = "files.upload",
            responseType = Result::class.java,
            request = params
        )
        if (log.isTraceEnabled) {
            log.trace("slack result:{}", result)
        }
        if (result?.ok != true) {
            log.error("slack api request fail:{}", result?.error)
        }
        return result?.ok == true
    }
}