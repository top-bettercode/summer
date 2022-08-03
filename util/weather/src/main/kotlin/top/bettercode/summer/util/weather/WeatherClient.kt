package top.bettercode.summer.util.weather

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.*
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.lang.Nullable
import top.bettercode.simpleframework.support.client.ApiTemplate
import top.bettercode.summer.util.weather.entity.WeatherResponse
import top.bettercode.summer.util.weather.entity.WeatherResult

/**
 * 天气接口
 *
 * @author Peter Wu
 */
open class WeatherClient(
    private val properties: WeatherProperties
) : ApiTemplate(
    "第三方平台", "天气数据", "weather", properties.connectTimeout, properties.readTimeout
) {

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
        val objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(messageConverter)
        super.setMessageConverters(messageConverters)
    }


    fun query(ip: String): WeatherResult {
        val entity: ResponseEntity<WeatherResponse> = try {
            execute(
                properties.url, HttpMethod.GET,
                null,
                responseEntityExtractor(WeatherResponse::class.java),
                ip,
                properties.appKey,
                properties.sign
            )
        } catch (e: Exception) {
            throw WeatherException(e)
        } ?: throw WeatherException()

        return if (entity.statusCode.is2xxSuccessful) {
            val body = entity.body
            if (body?.isOk() == true && body.result != null) {
                body.result
            } else {
                val message = body?.msg
                throw WeatherSysException(message ?: "请求失败")
            }
        } else {
            throw WeatherException()
        }
    }


}