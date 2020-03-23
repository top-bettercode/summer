package cn.bestwu.logging.bearychat

import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

/**
 *
 * @author Peter Wu
 */
class BearychatClient(private val webhookUrl: String,private val logUrl:String?) {

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
    fun postMessage(channel: String, title: String, initialComment: String, message: List<String>): Boolean {
        val params = mutableMapOf<String, Any>()
        params["markdown"] = true
        params["channel"] = channel
        params["notification"] = "$title: $initialComment"
        params["text"] = """$title
            
** $initialComment **
            
````````````````````````````````````````````````````````````````````````text
${message.joinToString("")}
````````````````````````````````````````````````````````````````````````
        """.trimIndent()
        if (logUrl != null) {
            params["attachments"] = arrayOf(mapOf("title" to "详细日志地址","url" to "$logUrl/logs/all.log"))
        }
        val result = restTemplate.postForObject(webhookUrl, params, Result::class.java)
        return result?.code == 0
    }


}