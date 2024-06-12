package top.bettercode.summer.logging.slack

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
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
import java.net.HttpURLConnection
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 *
 * <pre>
 * 请求ssl异常：sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
 * 处理方法：
 * 获取证书：
 *
 * bash
 *
 * echo | openssl s_client -connect slack.com:443 | openssl x509 > slack-com.crt
 *
 * 导入证书到 Java 信任库：
 *
 * bash
 *
 * keytool -import -alias slack-com -keystore $JAVA_HOME/jre/lib/security/cacerts -file slack-com.crt
 *
 * 默认密码是 changeit。
 *
 * </pre>
 * @author Peter Wu
 */
class SlackClient(
    private val authToken: String,
    private val logsPath: String,
    private val managementLogPath: String
) {

    private val api = "https://slack.com/api/"
    private val log: Logger = LoggerFactory.getLogger(SlackClient::class.java)
    private val restTemplate: RestTemplate = RestTemplate()
    var useCustomKeyStore = false

    init {
        val clientHttpRequestFactory = object : SimpleClientHttpRequestFactory() {

            override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
                if (useCustomKeyStore) {
                    val keyStore = KeyStore.getInstance("PKCS12")
                    val certFileResource = ClassPathResource("/slack-com.p12")
                    keyStore.load(certFileResource.inputStream, "changeit".toCharArray())

                    val keyManagerFactory =
                        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                    keyManagerFactory.init(keyStore, null)

                    val trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) {
                        }

                        override fun checkServerTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return emptyArray()
                        }
                    }

                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(
                        keyManagerFactory.keyManagers,
                        arrayOf(trustManager),
                        java.security.SecureRandom()
                    )
                    val httpsConnection = connection as HttpsURLConnection
                    val socketFactory = sslContext.socketFactory

                    httpsConnection.sslSocketFactory = socketFactory

                    httpsConnection.setHostnameVerifier { _: String?, _: SSLSession? -> true }
                }

                super.prepareConnection(connection, httpMethod)
            }
        }
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
        message: List<String>
    ): Boolean {
        val params = LinkedMultiValueMap<String, Any>()
        params.add("token", authToken)
        params.add("channel", channel)

        val actuatorAddress = try {
            top.bettercode.summer.logging.LoggingUtil.actuatorAddress
        } catch (e: Exception) {
            null
        }
        if (actuatorAddress == null) {
            return filesUpload(channel, timeStamp, title, initialComment, message)
        } else {
            params["text"] = "$title:\n$initialComment"

            val anchor = PrettyMessageHTMLLayout.anchor(message.last())
            val path = File(logsPath)
            val namePattern = "all-${TimeUtil.now().format("yyyy-MM-dd")}-"
            val files =
                path.listFiles { file -> file.name.startsWith(namePattern) && file.extension == "gz" }
            files?.sortBy { -it.lastModified() }
            val existFilename = files?.firstOrNull()?.nameWithoutExtension

            val filename = "$namePattern${
                if (existFilename != null) {
                    existFilename.substringAfter(namePattern).toInt() + 1
                } else {
                    0
                }
            }"

            val linkTitle = "${filename}.gz#$anchor"

            val logUrl = actuatorAddress + managementLogPath
            params["attachments"] = arrayOf(
                mapOf(
                    "title" to linkTitle,
                    "title_link" to "$logUrl/${linkTitle}"
                )
            )

            if (log.isTraceEnabled) {
                log.trace("slack params:{}", params)
            }

            val result = request(
                url = "chat.postMessage",
                responseType = Result::class.java,
                request = params
            )
            if (log.isTraceEnabled) {
                log.trace("slack result:{}", result)
            }
            return result?.ok == true
        }
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