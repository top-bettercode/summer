package cn.bestwu.logging.bearychat

import cn.bestwu.lang.util.RandomUtil
import cn.bestwu.logging.dateFileFormat
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 */
class BearychatClient(private val webhookUrl: String, private val logUrl: String?) {

    private val restTemplate: RestTemplate = RestTemplate()

    init {
        val clientHttpRequestFactory = SimpleClientHttpRequestFactory()
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(2000)
        //Read timeout
        clientHttpRequestFactory.setReadTimeout(10000)
        restTemplate.requestFactory = clientHttpRequestFactory
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
            if (message.isNotEmpty()) {
                val fileName = getFileName(filesPath!!)
                File(filesPath, fileName).writeText(message.joinToString(""))

                params["text"] = initialComment
                params["attachments"] = arrayOf(mapOf("title" to title, "url" to "$logUrl/logs/${fileName}"))
            } else {
                params["text"] = initialComment
                params["attachments"] = arrayOf(mapOf("title" to title))
            }
        }
        if (!hasFilesPath && logUrl != null) {
            params["attachments"] = arrayOf(mapOf("title" to "详细日志地址", "url" to "$logUrl/logs/all.log"))
        }

        val result = restTemplate.postForObject(webhookUrl, params, Result::class.java)
        return result?.code == 0
    }

    private fun getFileName(filesPath: String, index: Int = 0): String {
        val fileName = "alarm/${dateFileFormat.format(Date())}.${index}.log"
        return if (File(filesPath, fileName).exists()) {
            getFileName(filesPath, index + 1)
        } else {
            fileName
        }
    }
}