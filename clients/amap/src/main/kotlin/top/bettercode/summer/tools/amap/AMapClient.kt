package top.bettercode.summer.tools.amap

import com.fasterxml.jackson.databind.DeserializationFeature
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.web.client.DefaultResponseErrorHandler
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.amap.entity.AMapGeoResp
import top.bettercode.summer.tools.amap.entity.AMapRegeoResp
import top.bettercode.summer.tools.amap.entity.DistanceResp
import top.bettercode.summer.tools.amap.entity.Extensions
import top.bettercode.summer.tools.lang.client.ApiTemplate

/**
 *
 * @author Peter Wu
 */
@LogMarker(AMapClient.MARKER)
open class AMapClient(properties: AMapProperties) : ApiTemplate<AMapProperties>(
    marker = MARKER,
    properties = properties
) {
    companion object {
        const val MARKER = "amap"
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

        messageConverter.objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(AllEncompassingFormHttpMessageConverter())
        messageConverters.add(messageConverter)
        this.messageConverters = messageConverters

        this.errorHandler = object : DefaultResponseErrorHandler() {
            override fun handleError(response: ClientHttpResponse) {}
        }
    }

    /**
     * 逆地理编码 https://lbs.amap.com/api/webservice/guide/api/georegeo
     */
    @JvmOverloads
    open fun regeo(location: String, extensions: Extensions = Extensions.BASE): AMapRegeoResp {
        //restapi.amap.com/v3/geocode/regeo?key=您的key&location=116.481488,39.990464&poitype=&radius=&extensions=base&batch=false&roadlevel=0
        val entity: ResponseEntity<AMapRegeoResp> =
            exchange(
                properties.url + "/geocode/regeo?key={0}&location={1}&extensions={2}",
                HttpMethod.GET,
                null,
                AMapRegeoResp::class.java,
                properties.key,
                location, extensions.name.lowercase()
            ) ?: throw clientException()
        return entity.body ?: throw clientException()
    }

    /**
     * 地理编码 https://lbs.amap.com/api/webservice/guide/api/georegeo
     */
    open fun geo(address: String): AMapGeoResp {
        //https://restapi.amap.com/v3/geocode/geo?address=北京市朝阳区阜通东大街6号&output=JSON&key=您的key
        val entity: ResponseEntity<AMapGeoResp> =
            exchange(
                properties.url + "/geocode/geo?key={0}&address={1}&output=JSON",
                HttpMethod.GET,
                null,
                AMapGeoResp::class.java,
                properties.key,
                address
            ) ?: throw clientException("请求失败")

        return entity.body ?: throw clientException()
    }

    open fun distance(origins: String, destination: String): DistanceResp {
        val entity: ResponseEntity<DistanceResp> =
            exchange(
                properties.url + "/distance?key={0}&origins={1}&destination={2}&output=JSON",
                HttpMethod.GET,
                null,
                DistanceResp::class.java,
                properties.key,
                origins,
                destination
            ) ?: throw clientException("请求失败")

        return entity.body ?: throw clientException()
    }

}