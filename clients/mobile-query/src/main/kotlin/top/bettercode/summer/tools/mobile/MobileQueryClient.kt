package top.bettercode.summer.tools.mobile

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.*
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.ApiTemplate
import top.bettercode.summer.tools.lang.util.TimeUtil
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
@LogMarker(MobileQueryClient.MARKER)
open class MobileQueryClient(
    properties: MobileQueryProperties
) : ApiTemplate<MobileQueryProperties>(
    marker = MARKER,
    properties = properties,
) {

    companion object {
        const val MARKER = "mobile-query"
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
        this.messageConverters = messageConverters
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

        val requestCallback = this.httpEntityCallback<QueryResponse>(
            HttpEntity(bodyParams, headers),
            QueryResponse::class.java
        )
        val entity: ResponseEntity<QueryResponse> =
            execute(
                properties.url, HttpMethod.POST,
                requestCallback,
                this.responseEntityExtractor(QueryResponse::class.java)
            ) ?: throw clientException()
        return entity.body ?: throw clientException()
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
        val sig: String = Base64.getEncoder().encodeToString(hash)
        return "hmac id=\"$secretId\", algorithm=\"hmac-sha1\", headers=\"x-date x-source\", signature=\"$sig\""
    }

}