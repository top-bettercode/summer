package top.bettercode.summer.tools.mobile

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.*
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.Base64Utils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.ApiTemplate
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.mobile.MobileQueryClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.mobile.entity.QueryResponse
import java.security.Key
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 手机号查询接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class MobileQueryClient(
        private val properties: MobileQueryProperties
) : ApiTemplate(
        collectionName = "第三方平台",
        name = "获取本机手机号码",
        logMarker = LOG_MARKER,
        timeoutAlarmSeconds = properties.timeoutAlarmSeconds,
        connectTimeoutInSeconds = properties.connectTimeout,
        readTimeoutInSeconds = properties.readTimeout
) {

    companion object {
        const val LOG_MARKER = "mobile-query"
    }

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
                object : MappingJackson2HttpMessageConverter() {
                    override fun canRead(mediaType: MediaType?): Boolean {
                        return true
                    }

                    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
                        return true
                    }
                }
        val objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(messageConverter)
        this.restTemplate.messageConverters = messageConverters
    }


    open fun query(token: String): QueryResponse {
        val source = "market"
        val secretId = properties.appId
        val secretKey = properties.appKey
        val datetime = TimeUtil.now(ZoneId.of("GMT"))
                .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US))
        val headers = HttpHeaders()
        headers["X-Source"] = source
        headers["X-Date"] = datetime
        headers["Authorization"] = calcAuthorization(source, secretId, secretKey, datetime)

        val bodyParams: MutableMap<String, String> = mutableMapOf()
        bodyParams["appId"] = secretId
        bodyParams["appKey"] = secretKey
        bodyParams["token"] = token

        val requestCallback = this.restTemplate.httpEntityCallback<QueryResponse>(
                HttpEntity(bodyParams, headers),
                QueryResponse::class.java
        )
        val entity: ResponseEntity<QueryResponse> = try {
            execute(
                    properties.url, HttpMethod.POST,
                    requestCallback,
                    this.restTemplate.responseEntityExtractor(QueryResponse::class.java)
            )
        } catch (e: Exception) {
            throw QueryException(e)
        } ?: throw QueryException()

        return if (entity.statusCode.is2xxSuccessful) {
            val body = entity.body
            if (body?.isOk() == true) {
                body
            } else {
                val message = body?.message
                throw QuerySysException(message ?: "请求失败")
            }
        } else {
            throw QueryException()
        }
    }

    private fun calcAuthorization(
            source: String,
            secretId: String,
            secretKey: String,
            datetime: String
    ): String {
        val signStr = "x-date: $datetime\nx-source: $source"
        val mac: Mac = Mac.getInstance("HmacSHA1")
        val sKey: Key = SecretKeySpec(secretKey.toByteArray(charset("UTF-8")), mac.algorithm)
        mac.init(sKey)
        val hash: ByteArray = mac.doFinal(signStr.toByteArray(charset("UTF-8")))
        val sig: String = Base64Utils.encodeToString(hash)
        return "hmac id=\"$secretId\", algorithm=\"hmac-sha1\", headers=\"x-date x-source\", signature=\"$sig\""
    }

}