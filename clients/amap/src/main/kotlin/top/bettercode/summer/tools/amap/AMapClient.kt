package top.bettercode.summer.tools.amap

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.web.client.DefaultResponseErrorHandler
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.amap.AMapClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.amap.entity.AMapGeo
import top.bettercode.summer.tools.amap.entity.AMapRegeo
import top.bettercode.summer.tools.amap.entity.Location
import top.bettercode.summer.tools.lang.client.ApiTemplate

/**
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class AMapClient(private val amapProperties: AMapProperties) : ApiTemplate(
        collectionName = "第三方平台",
        name = "高德地图",
        logMarker = "amap",
        timeoutAlarmSeconds = amapProperties.timeoutAlarmSeconds,
        connectTimeout = amapProperties.connectTimeout,
        readTimeout = amapProperties.readTimeout
) {
    companion object {
        const val LOG_MARKER = "amap"
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
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(AllEncompassingFormHttpMessageConverter())
        messageConverters.add(messageConverter)
        this.restTemplate.messageConverters = messageConverters

        this.restTemplate.errorHandler = object : DefaultResponseErrorHandler() {
            override fun handleError(response: ClientHttpResponse) {}
        }
    }

    open fun regeo(location: Location): AMapRegeo {
        val requestCallback = this.restTemplate.httpEntityCallback<AMapRegeo>(
                HttpEntity(null, null),
                AMapRegeo::class.java
        )
        //restapi.amap.com/v3/geocode/regeo?key=您的key&location=116.481488,39.990464&poitype=&radius=&extensions=base&batch=false&roadlevel=0
        val expanded = restTemplate.uriTemplateHandler.expand(
                amapProperties.url + "/geocode/regeo?key={0}&location={1}",
                amapProperties.key,
                location.toString()
        )
        val entity: ResponseEntity<AMapRegeo> =
                execute(
                        expanded,
                        HttpMethod.GET,
                        requestCallback,
                        this.restTemplate.responseEntityExtractor(AMapRegeo::class.java)
                ) ?: throw AMapException("请求失败")
        val body = entity.body
        return if (body?.isOk == true) {
            body
        } else {
            throw AMapSysException(body?.info ?: "请求失败")
        }
    }

    open fun geo(address: String): AMapGeo {
        val requestCallback = this.restTemplate.httpEntityCallback<AMapGeo>(
                HttpEntity(null, null),
                AMapGeo::class.java
        )
        //https://restapi.amap.com/v3/geocode/geo?address=北京市朝阳区阜通东大街6号&output=JSON&key=您的key
        val expanded = restTemplate.uriTemplateHandler.expand(
                amapProperties.url + "/geocode/geo?key={0}&address={1}&output=JSON",
                amapProperties.key,
                address
        )
        val entity: ResponseEntity<AMapGeo> =
                execute(
                        expanded,
                        HttpMethod.GET,
                        requestCallback,
                        this.restTemplate.responseEntityExtractor(AMapGeo::class.java)
                ) ?: throw AMapException("请求失败")
        val body = entity.body
        return if (body?.isOk == true) {
            body
        } else {
            throw AMapSysException(body?.info ?: "请求失败")
        }
    }
}