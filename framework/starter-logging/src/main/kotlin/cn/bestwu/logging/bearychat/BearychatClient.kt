package cn.bestwu.logging.bearychat

import cn.bestwu.logging.formatNow
import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 */
class BearychatClient(private val webhookUrl: String, private val logUrl: String?) {

    private val log: Logger = LoggerFactory.getLogger(BearychatClient::class.java)
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

    /**
     * @param channel channel id or channel name
     */
    fun postMessage(channel: String, title: String, initialComment: String, message: List<String>, filesPath: String?): Boolean {
        val params = mutableMapOf<String, Any>()
        params["markdown"] = true
        params["channel"] = channel
        params["notification"] = initialComment

        val hasFilesPath = !filesPath.isNullOrBlank()
        if (!hasFilesPath || logUrl == null) {
            params["text"] = """$initialComment
            
 $title
````````````````````````````````````````````````````````````````````````text
${message.joinToString("")}
````````````````````````````````````````````````````````````````````````
        """.trimIndent()
        } else {
            params["text"] = initialComment
            if (message.isNotEmpty()) {
                val fileName = getFileName(filesPath!!)
                File(filesPath, fileName).writeText(message.joinToString(""))

                params["attachments"] = arrayOf(mapOf("title" to title, "url" to "$logUrl/logs/${fileName}"))
            } else {
                params["attachments"] = arrayOf(mapOf("title" to title, "url" to "$logUrl/logs/all.log"))
            }
        }
        if (!hasFilesPath && logUrl != null) {
            params["attachments"] = arrayOf(mapOf("title" to "详细日志地址", "url" to "$logUrl/logs/all.log"))
        }

        if (log.isDebugEnabled) {
            log.debug("bearychat params:{}", params)
        }

        val result = restTemplate.postForObject(webhookUrl, params, Result::class.java)
        return result?.code == 0
    }

    private fun getFileName(filesPath: String, index: Int = 0): String {
        val fileName = "alarm/${formatNow()}.${index}.log"
        return if (File(filesPath, fileName).exists()) {
            getFileName(filesPath, index + 1)
        } else {
            fileName
        }
    }
}