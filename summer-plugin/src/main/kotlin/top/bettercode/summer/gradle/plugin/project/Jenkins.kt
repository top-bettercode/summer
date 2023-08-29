package top.bettercode.summer.gradle.plugin.project

import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject
import top.bettercode.summer.tools.lang.capitalized

/**
 *
 * @author Peter Wu
 */
class Jenkins(private val url: String, auth: String) {
    private val log = LoggerFactory.getLogger(Jenkins::class.java)
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

    fun build(jobName: String, env: String = "default") {
        restTemplate.postForEntity("$url/job/${jobName}/build", null, String::class.java)
        log.warn("已发送build请求...")
        val description = description(jobName)
        if (description.isNotBlank()) {
            log.warn("job 描述信息：")
            log.warn(description)
        }
        val envName = if (env == "default") "" else env.capitalized()
        val jobTaskName = jobName.replace(
                "[()\\[\\]{}|/]|\\s*|\t|\r|\n|".toRegex(),
                ""
        ).capitalized()
        log.warn("如需查看最新build信息，请运行:lastBuildInfo$envName$jobTaskName 任务")
    }

    fun buildInfo(job: String, id: String = "lastBuild", startIndex: Int = 0) {
        //X-Text-Size: 2565
        //X-More-Data: true
        val entity =
                restTemplate.getForEntity<String>("$url/job/${job}/$id/logText/progressiveText")
        val body = entity.body ?: ""
        val message = body.substring(startIndex)
        if (message.isNotBlank())
            log.warn(message)
        val headers = entity.headers
        val hasMoreData = headers["X-More-Data"]?.firstOrNull()?.toBoolean() == true
        if (hasMoreData) {
            Thread.sleep(3 * 1000)
            buildInfo(job, id, body.length)
        }
    }
}