package top.bettercode.summer.tools.jpush

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.*
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientResponseException
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.jpush.entity.JpushRequest
import top.bettercode.summer.tools.jpush.entity.Options
import top.bettercode.summer.tools.jpush.entity.resp.JpushCidResponse
import top.bettercode.summer.tools.jpush.entity.resp.JpushErrorResponse
import top.bettercode.summer.tools.jpush.entity.resp.JpushResponse
import top.bettercode.summer.tools.lang.client.ApiTemplate

/**
 * 推送接口
 *
 * @author Peter Wu
 */
@LogMarker(JpushClient.MARKER)
open class JpushClient(
    properties: JpushProperties
) : ApiTemplate<JpushProperties>(
    marker = MARKER,
    properties = properties
) {

    companion object {
        const val MARKER = "jpush"
    }

    var objectMapper: ObjectMapper

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
        objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(messageConverter)
        this.messageConverters = messageConverters
    }


    open fun send(request: JpushRequest): JpushResponse {
        val headers = HttpHeaders()
        headers.setBasicAuth(properties.appKey, properties.masterSecret)
        val requestCallback = this.httpEntityCallback<JpushResponse>(
            HttpEntity(request, headers),
            JpushResponse::class.java
        )
        if (request.cid == null) {
            val cidlist = cid(1).cidlist
            if (cidlist?.isNotEmpty() == true) {
                request.cid = cidlist[0]
            } else {
                throw clientException("获取cid失败")
            }
        }
        request.options = Options(properties.timeToLive, properties.apnsProduction)
        val entity: ResponseEntity<JpushResponse> = try {
            execute(
                properties.url + "/push", HttpMethod.POST,
                requestCallback,
                this.responseEntityExtractor(JpushResponse::class.java)
            )
        } catch (e: Exception) {
            if (e is RestClientResponseException) {
                val errorResponse = objectMapper.readValue(
                    e.responseBodyAsByteArray,
                    JpushErrorResponse::class.java
                )
                val error = errorResponse.error
                if (error != null) {
                    throw clientSysException(error.message, error)
                }
            }
            throw clientException(e)
        } ?: throw clientException()

        return entity.body ?: throw clientException()
    }

    /**
     * @param count 可选参数。数值类型，不传则默认为 1。范围为 [1, 1000]
     */
    open fun cid(count: Int): JpushCidResponse {
        val headers = HttpHeaders()
        headers.setBasicAuth(properties.appKey, properties.masterSecret)
        val requestCallback = this.httpEntityCallback<JpushCidResponse>(
            HttpEntity(null, headers),
            JpushCidResponse::class.java
        )
        val entity: ResponseEntity<JpushCidResponse> = try {
            execute(
                properties.url + "/push/cid?count={0}", HttpMethod.GET,
                requestCallback,
                this.responseEntityExtractor(JpushCidResponse::class.java),
                count
            )
        } catch (e: Exception) {
            if (e is RestClientResponseException) {
                val errorResponse = objectMapper.readValue(
                    e.responseBodyAsByteArray,
                    JpushErrorResponse::class.java
                )
                val error = errorResponse.error
                if (error != null) {
                    throw clientSysException(error.message, error)
                }
            }
            throw clientException(e)
        } ?: throw clientException()

        return entity.body ?: throw clientException()
    }


}