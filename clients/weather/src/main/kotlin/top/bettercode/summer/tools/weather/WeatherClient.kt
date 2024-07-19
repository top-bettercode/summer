package top.bettercode.summer.tools.weather

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.ApiTemplate
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.weather.entity.WeatherResponse
import top.bettercode.summer.tools.weather.entity.WeatherResult
import top.bettercode.summer.tools.weather.entity.WeatherType
import java.time.LocalTime

/**
 * 天气接口
 *
 * @author Peter Wu
 */
@LogMarker(WeatherClient.MARKER)
open class WeatherClient(
    properties: WeatherProperties
) : ApiTemplate<WeatherProperties>(
    marker = MARKER,
    properties = properties
) {
    companion object {
        const val MARKER = "weather"
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

    @JvmOverloads
    open fun isNight(
        time: LocalTime = LocalTime.now(TimeUtil.DEFAULT_ZONE_ID)
    ): Boolean {
        return properties.nightStartTime.hour <= time.hour && time.hour <= properties.nightEndTime.hour
    }

    private val weatherTypeType =
        object : ParameterizedTypeReference<WeatherResponse<Map<String, WeatherType>>>() {}

    /**
     * <a href="https://www.nowapi.com/api/weather.wtype">天气类型</a>
     */
    open fun type(): Map<String, WeatherType> {
        val entity: ResponseEntity<WeatherResponse<Map<String, WeatherType>>> =
            exchange(
                properties.url + "/?app=weather.wtype&appkey={0}&sign={1}&format=json",
                HttpMethod.GET,
                null,
                weatherTypeType,
                properties.appKey,
                properties.sign
            ) ?: throw clientException()
        return entity.body?.result ?: throw clientException(entity.body?.msg)
    }

    private val weatherResultType =
        object : ParameterizedTypeReference<WeatherResponse<WeatherResult>>() {}

    /**
     * <a href="https://www.nowapi.com/api/weather.realtime">天气接口文档</a>
     */
    open fun query(ip: String): WeatherResult {
        val entity: ResponseEntity<WeatherResponse<WeatherResult>> =
            exchange(
                properties.url + "/?app=weather.realtime&appkey={0}&sign={1}&format=json&cityIp={2}",
                HttpMethod.GET,
                null,
                weatherResultType,
                properties.appKey,
                properties.sign,
                ip
            )
                ?: throw clientException()

        return entity.body?.result ?: throw clientException(entity.body?.msg)
    }

    /**
     * <a href="https://www.nowapi.com/api/weather.realtime">天气接口文档</a>
     */
    open fun query(longitude: Double, latitude: Double): WeatherResult {
        val entity: ResponseEntity<WeatherResponse<WeatherResult>> =
            exchange(
                properties.url + "/?app=weather.realtime&appkey={0}&sign={1}&format=json&wgs84ll={2},{3}",
                HttpMethod.GET,
                null,
                weatherResultType,
                properties.appKey,
                properties.sign,
                longitude,
                latitude
            ) ?: throw clientException()
        return entity.body?.result ?: throw clientException(entity.body?.msg)
    }

}