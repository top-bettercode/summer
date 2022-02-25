package plugin

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject
import java.net.URLEncoder

/**
 *
 * @author Peter Wu
 */
class Jenkins(private val url: String, auth: String) {
    private val restTemplate: RestTemplate

    init {
        val split = auth.split(":")
        val builder = RestTemplateBuilder().basicAuthentication(split[0], split[1])
        restTemplate = builder.errorHandler(object : DefaultResponseErrorHandler() {
            override fun handleError(response: ClientHttpResponse) {}
        }).build()
    }

    fun description(job: String): String {
        return restTemplate.getForObject(
            "$url/job/${URLEncoder.encode(job, "UTF-8")}/description"
        )
    }

    fun build(job: String) {
        restTemplate.postForEntity(
            "$url/job/${URLEncoder.encode(job, "UTF-8")}/build",
            null,
            String::class.java
        )
        println("已发送build请求...")
        val description = description(job)
        if (description.isNotBlank()) {
            println("job 描述信息：")
            println(description)
        }
        println(
            "如需查看最新build信息，请运行:lastBuildInfo[${
                job.replace(
                    "[()\\[\\]{}|/]|\\s*|\t|\r|\n|".toRegex(),
                    ""
                )
            }] 任务"
        )
    }

    fun buildInfo(job: String, id: String = "lastBuild", startIndex: Int = 0) {
        //X-Text-Size: 2565
        //X-More-Data: true
        val entity = restTemplate.getForEntity<String>(
            "$url/job/${URLEncoder.encode(job, "UTF-8")}/$id/logText/progressiveText"
        )
        val body = entity.body ?: ""
        val message = body.substring(startIndex)
        if (message.isNotBlank())
            println(message)
        val headers = entity.headers
        val hasMoreData = headers["X-More-Data"]?.firstOrNull()?.toBoolean() == true
        if (hasMoreData) {
            Thread.sleep(3 * 1000)
            buildInfo(job, id, body.length)
        }
    }
}