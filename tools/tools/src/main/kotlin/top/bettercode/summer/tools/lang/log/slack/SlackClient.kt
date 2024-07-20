package top.bettercode.summer.tools.lang.log.slack

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.web.client.RestTemplate
import top.bettercode.summer.tools.lang.log.AlarmAppender.Companion.NO_ALARM_LOG_MARKER
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.lang.util.TimeUtil.Companion.DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN
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
    private val authToken: String
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
        headers.set("Authorization", "Bearer $authToken")
        val requestEntity = HttpEntity(request, headers)
        val response = restTemplate.exchange(api + url, method, requestEntity, responseType)
        return response.body
    }

    /**
     * https://api.slack.com/methods/conversations.list
     */
    fun channelsList(): List<Channel>? {
        val result = request(
            url = "conversations.list?types=public_channel,private_channel&exclude_archived=true",
            responseType = ChannelsResult::class.java,
            method = HttpMethod.GET
        )
        if (result?.ok != true) {
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "slack api request fail:{}",
                result?.error
            )
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
     * https://api.slack.com/methods/chat.postMessage
     *
     * @param channel channel id or channel name
     */
    fun postMessage(
        channel: String,
        title: String,
        initialComment: String,
        logUrl: String,
        linkTitle: String
    ): Boolean {
        val params = mapOf(
            "token" to authToken,
            "channel" to channel,
            "text" to "$title:\n$initialComment",
            "attachments" to arrayOf(
                mapOf(
                    "title" to linkTitle,
                    "title_link" to "$logUrl/${linkTitle}"
                )
            )
        )

        if (log.isTraceEnabled) {
            log.trace("slack params:{}", params)
        }

        val result = request(
            url = "chat.postMessage",
            responseType = SlackResult::class.java,
            request = params
        )
        if (log.isTraceEnabled) {
            log.trace("slack result:{}", result)
        }
        if (result?.ok != true) {
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "slack api request fail:{}",
                result?.error
            )
        }
        return result?.ok == true
    }

    /**
     * https://api.slack.com/methods/files.upload
     *
     */
    fun filesUpload(
        channel: String,
        timeStamp: Long,
        title: String,
        initialComment: String,
        message: List<String>
    ): Boolean {
        val params = mutableMapOf<String, Any>()
        params["token"] = authToken
        params["channels"] = channel
        params["content"] = message.joinToString("\n").toByteArray()
        params["filename"] =
            "$title-${TimeUtil.format(timeStamp, DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN)}.log"
        params["filetype"] = "text"
        if (title.isNotBlank()) {
            params["title"] =
                "$title-${TimeUtil.format(timeStamp, DEFAULT_DATE_TIME_SSS_FORMAT_PATTERN)}"
        }
        params["initial_comment"] = "$title:\n$initialComment"
        val result = request(
            url = "files.upload",
            responseType = SlackResult::class.java,
            request = params
        )
        if (log.isTraceEnabled) {
            log.trace("slack result:{}", result)
        }
        if (result?.ok != true) {
            log.error(
                MarkerFactory.getMarker(NO_ALARM_LOG_MARKER),
                "slack api request fail:{}",
                result?.error
            )
        }
        return result?.ok == true
    }
}