package top.bettercode.summer.tools.weather

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.type.TypeFactory
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.TimeUtil
import top.bettercode.summer.tools.weather.WeatherClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.weather.entity.WeatherResponse
import top.bettercode.summer.tools.weather.entity.WeatherResult
import top.bettercode.summer.tools.weather.entity.WeatherType
import top.bettercode.summer.web.support.client.ApiTemplate
import java.time.LocalTime

/**
 * 天气接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class WeatherClient(
        private val properties: WeatherProperties
) : ApiTemplate(
        "第三方平台", "天气数据", LOG_MARKER, properties.connectTimeout, properties.readTimeout
) {
    companion object {
        const val LOG_MARKER = "weather"
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
        super.setMessageConverters(messageConverters)
    }


    @JvmOverloads
    fun isNight(
            time: LocalTime = LocalTime.now(TimeUtil.DEFAULT_ZONE_ID)
    ): Boolean {
        return properties.nightStartTime.hour <= time.hour && time.hour <= properties.nightEndTime.hour
    }

    fun type(): Map<String, WeatherType> {
        val defaultInstance = TypeFactory.defaultInstance()
        val javaType = defaultInstance.constructParametricType(
                WeatherResponse::class.java,
                defaultInstance.constructMapType(
                        Map::class.java,
                        String::class.java,
                        WeatherType::class.java
                )
        )
        val entity: ResponseEntity<WeatherResponse<Map<String, WeatherType>>> = try {
            execute(
                    properties.url + "/?app=weather.wtype&appkey={0}&sign={1}&format=json",
                    HttpMethod.GET,
                    null,
                    responseEntityExtractor(javaType),
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

    fun query(ip: String): WeatherResult {
        val javaType = TypeFactory.defaultInstance().constructParametricType(
                WeatherResponse::class.java, WeatherResult::class.java
        )
        val entity: ResponseEntity<WeatherResponse<WeatherResult>> = try {
            execute(
                    properties.url + "/?app=weather.realtime&appkey={0}&sign={1}&format=json&cityIp={2}",
                    HttpMethod.GET,
                    null,
                    responseEntityExtractor(javaType),
                    properties.appKey,
                    properties.sign,
                    ip
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

    fun query(longitude: Double, latitude: Double): WeatherResult {
        val javaType = TypeFactory.defaultInstance().constructParametricType(
                WeatherResponse::class.java, WeatherResult::class.java
        )
        val entity: ResponseEntity<WeatherResponse<WeatherResult>> = try {
            execute(
                    properties.url + "/?app=weather.realtime&appkey={0}&sign={1}&format=json&wgs84ll={2},{3}",
                    HttpMethod.GET,
                    null,
                    responseEntityExtractor(javaType),
                    properties.appKey,
                    properties.sign,
                    longitude,
                    latitude
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