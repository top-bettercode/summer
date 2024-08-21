package top.bettercode.summer.gradle.plugin.project

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.*
import org.w3c.dom.Element
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


/**
 *
 * @author Peter Wu
 */
class Jenkins(private val url: String, auth: String) {
    private val log = LoggerFactory.getLogger(Jenkins::class.java)
    private val restTemplate: RestTemplate by lazy {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add { request, body, execution ->
            val headers = request.headers
            val (username, password) = auth.split(":")
            headers.setBasicAuth(username, password)
            execution.execute(request, body)
        }
        restTemplate.errorHandler = (object : DefaultResponseErrorHandler() {
            override fun handleError(response: ClientHttpResponse) {}
        })

        (restTemplate.messageConverters.find { it is StringHttpMessageConverter } as StringHttpMessageConverter?)?.defaultCharset =
            StandardCharsets.UTF_8

        restTemplate
    }

    fun config(job: String): String? {
        return restTemplate.getForObject("$url/job/${job}/config.xml")
    }

    fun updateConfig(job: String, config: ByteArray) {
        val headers = HttpHeaders()
        headers.set(
            "Content-Type",
            MediaType(MediaType.TEXT_XML, Charset.forName("UTF-8")).toString()
        )
        val response: String =
            restTemplate.postForObject("$url/job/${job}/config.xml", HttpEntity(config, headers))
                ?: "成功"
        log.warn("更新[$job]配置结果:$response")
    }

    fun currentBranch(): String? {
        val processBuilder = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
        processBuilder.directory(File("./"))

        return try {
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val branch = reader.readText().trim()
            process.waitFor()
            branch
        } catch (e: Exception) {
            null
        }
    }

    fun changeBranch(job: String, branch: String) {
        val config = config(job)
        if (config.isNullOrBlank()) {
            log.warn("$job 配置获取失败")
            return
        }
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.parse(config.byteInputStream())
        doc.documentElement.normalize()

        // 修改 <name> 节点的值
        val branchSpecNodes = doc.getElementsByTagName("hudson.plugins.git.BranchSpec")
        for (i in 0 until branchSpecNodes.length) {
            val branchSpecNode = branchSpecNodes.item(i) as Element
            val nameNodes = branchSpecNode.getElementsByTagName("name")
            if (nameNodes.length > 0) {
                val nameNode = nameNodes.item(0)
                val remoteBranch = nameNode.textContent.trim()
                log.warn("当前jenkins代码分支：$remoteBranch")
                if (remoteBranch == branch) {
                    return
                }
                nameNode.textContent = branch
            }
        }
        // 将修改后的 XML 转换为字符串
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        val source = DOMSource(doc)
        val outputStream = ByteArrayOutputStream()
        val result = StreamResult(outputStream)
        transformer.transform(source, result)

        updateConfig(job, outputStream.toByteArray())
        log.warn("修改[$job]代码分支为：$branch")
    }

    fun description(job: String): String {
        return restTemplate.getForObject("$url/job/${job}/description") ?: ""
    }

    fun build(jobName: String, lastBuildInfoTaskName: String? = null) {
        currentBranch()?.let { branch ->
            log.warn("当前分支：$branch")
            changeBranch(jobName, branch)
        }

        restTemplate.postForEntity("$url/job/${jobName}/build", null, String::class.java)
        log.warn("\n已发送build请求...")
        val description = description(jobName)
        if (description.isNotBlank()) {
            log.warn("job 描述信息：")
            log.warn(description)
        }
        if (!lastBuildInfoTaskName.isNullOrBlank())
            log.warn("\n如需查看最新build信息，请运行:[$lastBuildInfoTaskName]任务")
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