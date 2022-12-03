package top.bettercode.summer.gradle.plugin.project

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject

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
        return restTemplate.getForObject("$url/job/${job}/description") ?: ""
    }

    fun build(job: String, env: String = "default") {
        restTemplate.postForEntity("$url/job/${job}/build", null, String::class.java)
        println("已发送build请求...")
        val description = description(job)
        if (description.isNotBlank()) {
            println("job 描述信息：")
            println(description)
        }
        val envName = if (env == "default") "" else "[$env]"
        println(
            "如需查看最新build信息，请运行:lastBuildInfo$envName[${
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
        val entity =
            restTemplate.getForEntity<String>("$url/job/${job}/$id/logText/progressiveText")
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