package cn.bestwu.logging.slack

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

/**
 *
 * @author Peter Wu
 */
class SlackClient(private val authToken: String) {

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
    }

    fun channelsList(): List<Channel>? {
        val result = restTemplate.getForObject("${api}channels.list?token=$authToken", Result::class.java)
        if (result?.ok != true) {
            log.error("slack api request fail:{}", result?.error)
        }
        return result?.channels
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
     * @param channel channel id
     * @return Channel?
     */
    fun channelInfo(channel: String): Channel? {
        val result = restTemplate.getForObject("${api}channels.info?token=$authToken&channel=$channel", Result::class.java)
        if (result?.ok != true) {
            log.error("slack api request fail:{}", result?.error)
        }
        return result?.channel
    }

    /**
     * @param channel channel id or channel name
     */
    fun postMessage(channel: String, message: String): Boolean {
        val params = LinkedMultiValueMap<String, Any>()
        params.add("token", authToken)
        params.add("channel", channel)
        params.add("text", message)
        val result = restTemplate.postForObject("${api}chat.postMessage", params, Result::class.java)
        return result?.ok == true
    }

    /**
     * @param channels Comma-separated list of channel names or IDs where the file will be shared.
     */
    fun filesUpload(channels: String, data: ByteArray, fileName: String, filetype: String, title: String?, initialComment: String): Boolean {
        val params = LinkedMultiValueMap<String, Any>()
        params.add("token", authToken)
        params.add("channels", channels)
        params.add("content", data)
        params.add("filename", fileName)
        params.add("filetype", filetype)
        if (!title.isNullOrBlank()) {
            params.add("title", title)
        }
        params.add("initial_comment", initialComment)
        val result = restTemplate.postForObject("${api}files.upload", params, Result::class.java)
        if (result?.ok != true) {
            log.error("slack api request fail:{}", result?.error)
        }
        return result?.ok == true
    }
}