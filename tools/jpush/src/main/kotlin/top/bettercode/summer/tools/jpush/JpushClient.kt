package top.bettercode.summer.tools.jpush

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.*
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.web.client.HttpClientErrorException
import top.bettercode.summer.tools.jpush.entity.JpushRequest
import top.bettercode.summer.tools.jpush.entity.Options
import top.bettercode.summer.tools.jpush.entity.resp.JpushCidResponse
import top.bettercode.summer.tools.jpush.entity.resp.JpushErrorResponse
import top.bettercode.summer.tools.jpush.entity.resp.JpushResponse
import top.bettercode.summer.web.support.client.ApiTemplate

/**
 * 推送接口
 *
 * @author Peter Wu
 */
open class JpushClient(
    private val properties: JpushProperties
) : ApiTemplate(
    "第三方平台", "极光推送", "jpush", properties.connectTimeout, properties.readTimeout
) {
    var objectMapper: ObjectMapper

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
            object : MappingJackson2HttpMessageConverter() {
                override fun canRead(@Nullable mediaType: MediaType?): Boolean {
                    return true
                }

                override fun canWrite(clazz: Class<*>, @Nullable mediaType: MediaType?): Boolean {
                    return true
                }
            }
        objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(messageConverter)
        super.setMessageConverters(messageConverters)

    }


    fun send(request: JpushRequest): JpushResponse {
        val headers = HttpHeaders()
        headers.setBasicAuth(properties.appKey, properties.masterSecret)
        val requestCallback = httpEntityCallback<Any>(
            HttpEntity(request, headers),
            JpushResponse::class.java
        )
        if (request.cid == null) {
            val cidlist = cid(1).cidlist
            if (cidlist?.isNotEmpty() == true) {
                request.cid = cidlist[0]
            } else {
                throw JpushException("获取cid失败")
            }
        }
        request.options = Options(properties.timeToLive, properties.isApnsProduction)
        val entity: ResponseEntity<JpushResponse> = try {
            execute(
                properties.url + "/push", HttpMethod.POST,
                requestCallback,
                responseEntityExtractor(JpushResponse::class.java)
            )
        } catch (e: Exception) {
            if (e is HttpClientErrorException) {
                val errorResponse = objectMapper.readValue(
                    e.responseBodyAsByteArray,
                    JpushErrorResponse::class.java
                )
                throw JpushSysException(errorResponse)
            }
            throw JpushException(e)
        } ?: throw JpushException()

        return if (entity.statusCode.is2xxSuccessful) {
            entity.body ?: throw JpushException("请求失败")
        } else {
            throw JpushException("请求失败")
        }
    }

    /**
     * @param count 可选参数。数值类型，不传则默认为 1。范围为 [1, 1000]
     */
    fun cid(count: Int): JpushCidResponse {
        val headers = HttpHeaders()
        headers.setBasicAuth(properties.appKey, properties.masterSecret)
        val requestCallback = httpEntityCallback<Any>(
            HttpEntity(null, headers),
            JpushCidResponse::class.java
        )
        val entity: ResponseEntity<JpushCidResponse> = try {
            execute(
                properties.url + "/push/cid?count={0}", HttpMethod.GET,
                requestCallback,
                responseEntityExtractor(JpushCidResponse::class.java),
                count
            )
        } catch (e: Exception) {
            if (e is HttpClientErrorException) {
                val errorResponse = objectMapper.readValue(
                    e.responseBodyAsByteArray,
                    JpushErrorResponse::class.java
                )
                throw JpushSysException(errorResponse)
            }
            throw JpushException(e)
        } ?: throw JpushException()

        return if (entity.statusCode.is2xxSuccessful) {
            entity.body ?: throw JpushException("请求失败")
        } else {
            throw JpushException("请求失败")
        }
    }


}