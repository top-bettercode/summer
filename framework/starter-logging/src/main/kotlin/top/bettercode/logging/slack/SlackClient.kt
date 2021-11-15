package top.bettercode.logging.slack

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import top.bettercode.lang.PrettyMessageHTMLLayout
import top.bettercode.lang.util.LocalDateTimeHelper
import top.bettercode.logging.RequestLoggingFilter
import java.io.File

/**
 *
 * @author Peter Wu
 */
class SlackClient(
    private val authToken: String,
    private val logAll: Boolean,
    private val managementPath: String
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

    fun channelsList(): List<Channel>? {
        val result = restTemplate.getForObject(
            "${api}conversations.list?token=$authToken&types=public_channel,private_channel&exclude_archived=true",
            ChannelsResult::class.java
        )
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
     * @param channel channel id or channel name
     */
    fun postMessage(
        channel: String,
        timeStamp: Long,
        title: String,
        initialComment: String,
        message: List<String>,
        logsPath: String?
    ): Boolean {
        val params = LinkedMultiValueMap<String, Any>()
        params.add("token", authToken)
        params.add("channel", channel)
        val hasFilesPath = !logsPath.isNullOrBlank()
        if (!hasFilesPath || RequestLoggingFilter.API_HOST == null) {
            return filesUpload(channel, timeStamp, title, initialComment, message)
        } else {
            params["text"] = "$title:\n$initialComment"

            val logUrl = RequestLoggingFilter.API_HOST + managementPath
            if (message.isNotEmpty()) {
                val anchor = PrettyMessageHTMLLayout.anchor(message.last())
                val fileName = "alarm/${anchor}.log"
                val linkTitle = "${fileName}#last"
                File(logsPath, fileName).writeText(message.joinToString(""))
                if (logAll) {
                    params["attachments"] = arrayOf(
                        mapOf(
                            "title" to linkTitle,
                            "title_link" to "$logUrl/logs/${fileName}#last"
                        ),
                        mapOf(
                            "title" to "all#$anchor",
                            "title_link" to "$logUrl/logs/all.log#$anchor"
                        )
                    )
                } else {
                    params["attachments"] =
                        arrayOf(
                            mapOf(
                                "title" to linkTitle,
                                "title_link" to "$logUrl/logs/${fileName}#last"
                            )
                        )
                }
            } else {
                if (logAll)
                    params["attachments"] =
                        arrayOf(
                            mapOf(
                                "title" to "all.log#last",
                                "title_link" to "$logUrl/logs/all.log#last"
                            )
                        )
            }
        }

        if (log.isDebugEnabled) {
            log.debug("slack params:{}", params)
        }

        val result =
            restTemplate.postForObject("${api}chat.postMessage", params, Result::class.java)
        if (log.isDebugEnabled) {
            log.debug("slack result:{}", result)
        }
        return result?.ok == true
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
        params.add("filename", "$title-${LocalDateTimeHelper.format(timeStamp)}.log")
        params.add("filetype", "text")
        if (title.isNotBlank()) {
            params.add("title", "$title-${LocalDateTimeHelper.format(timeStamp)}")
        }
        params.add("initial_comment", "$title:\n$initialComment")
        val result = restTemplate.postForObject("${api}files.upload", params, Result::class.java)
        if (log.isDebugEnabled) {
            log.debug("slack result:{}", result)
        }
        if (result?.ok != true) {
            log.error("slack api request fail:{}", result?.error)
        }
        return result?.ok == true
    }
}