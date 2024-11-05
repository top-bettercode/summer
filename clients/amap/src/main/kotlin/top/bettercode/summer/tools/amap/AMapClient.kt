package top.bettercode.summer.tools.amap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.web.client.DefaultResponseErrorHandler
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.amap.entity.*
import top.bettercode.summer.tools.lang.client.ApiTemplate
import java.util.concurrent.TimeUnit

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

    private val regeoCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .maximumSize(100).build<String, AMapRegeoResp>().asMap()
    private val geoCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .maximumSize(100).build<String, AMapGeoResp>().asMap()
    private val distanceCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .maximumSize(100).build<String, DistanceResp>().asMap()
    private val poiSearchCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .maximumSize(100).build<String, AMapPoiResp>().asMap()

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
        val key = "regeo::$location::$extensions"
        return regeoCache.computeIfAbsent(key) {
            //restapi.amap.com/v3/geocode/regeo?key=您的key&location=116.481488,39.990464&poitype=&radius=&extensions=base&batch=false&roadlevel=0
            val entity: ResponseEntity<AMapRegeoResp> =
                exchange(
                    properties.url + "/v3/geocode/regeo?key={0}&location={1}&extensions={2}",
                    HttpMethod.GET,
                    null,
                    AMapRegeoResp::class.java,
                    properties.key,
                    location, extensions.name.lowercase()
                ) ?: throw clientException()
            return@computeIfAbsent entity.body ?: throw clientException()
        }
    }

    /**
     * 关键字搜索 https://lbs.amap.com/api/webservice/guide/api-advanced/newpoisearch
     */
    @JvmOverloads
    open fun poiSearch(
        keywords: String,
        region: String? = null,
        types: String? = null
    ): AMapPoiResp {
        //https://restapi.amap.com/v5/place/text?keywords=北京大学&types=141201&region=北京市&key=<用户的key>
        val key = "poiSearch::$keywords::$region"
        return poiSearchCache.computeIfAbsent(key) {
            val entity: ResponseEntity<AMapPoiResp> =
                exchange(
                    properties.url + "/v5/place/text?key={0}&keywords={1}&types={3}&region={2}&output=JSON",
                    HttpMethod.GET,
                    null,
                    AMapPoiResp::class.java,
                    properties.key,
                    keywords, region, types
                ) ?: throw clientException("请求失败")
            return@computeIfAbsent entity.body ?: throw clientException()
        }
    }

    /**
     * 地理编码 https://lbs.amap.com/api/webservice/guide/api/georegeo
     */
    open fun geo(address: String): AMapGeoResp {
        val key = "geo::$address"
        return geoCache.computeIfAbsent(key) {
            //https://restapi.amap.com/v3/geocode/geo?address=北京市朝阳区阜通东大街6号&output=JSON&key=您的key
            val entity: ResponseEntity<AMapGeoResp> =
                exchange(
                    properties.url + "/v3/geocode/geo?key={0}&address={1}&output=JSON",
                    HttpMethod.GET,
                    null,
                    AMapGeoResp::class.java,
                    properties.key,
                    address
                ) ?: throw clientException("请求失败")

            return@computeIfAbsent entity.body ?: throw clientException()
        }
    }

    open fun distance(origins: String, destination: String): DistanceResp {
        val key = "distance::$origins::$destination"
        return distanceCache.computeIfAbsent(key) {
            val entity: ResponseEntity<DistanceResp> =
                exchange(
                    properties.url + "/v3/distance?key={0}&origins={1}&destination={2}&output=JSON",
                    HttpMethod.GET,
                    null,
                    DistanceResp::class.java,
                    properties.key,
                    origins,
                    destination
                ) ?: throw clientException("请求失败")

            return@computeIfAbsent entity.body ?: throw clientException()
        }
    }

}