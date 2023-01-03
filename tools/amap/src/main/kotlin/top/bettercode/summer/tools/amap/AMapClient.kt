package top.bettercode.summer.tools.amap

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.web.client.DefaultResponseErrorHandler
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.amap.AMapClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.amap.entity.Location
import top.bettercode.summer.tools.amap.entity.AmapRegeo
import top.bettercode.summer.web.support.client.ApiTemplate

/**
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
class AMapClient(private val amapProperties: AmapProperties) : ApiTemplate(
    "第三方平台", "高德地图", "amap", amapProperties.connectTimeout,
    amapProperties.readTimeout
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

                override fun canWrite(clazz: Class<*>, @Nullable mediaType: MediaType?): Boolean {
                    return true
                }
            }
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(AllEncompassingFormHttpMessageConverter())
        messageConverters.add(messageConverter)
        setMessageConverters(messageConverters)

        errorHandler = object : DefaultResponseErrorHandler() {
            override fun handleError(response: ClientHttpResponse) {}
        }
    }

    fun regeo(location: Location): AmapRegeo {
        val requestCallback = httpEntityCallback<Any>(
            HttpEntity(null, null),
            AmapRegeo::class.java
        )
        //restapi.amap.com/v3/geocode/regeo?key=您的key&location=116.481488,39.990464&poitype=&radius=&extensions=base&batch=false&roadlevel=0
        val expanded = uriTemplateHandler.expand(
            amapProperties.url + "/geocode/regeo?key={0}&location={1}",
            amapProperties.key,
            location.toString()
        )
        val entity: ResponseEntity<AmapRegeo> =
            execute(
                expanded,
                HttpMethod.GET,
                requestCallback,
                responseEntityExtractor(AmapRegeo::class.java)
            ) ?: throw AmapException("请求失败")
        val body = entity.body
        return if (body?.isOk == true) {
            body
        } else {
            throw AmapSysException(body?.info ?: "请求失败")
        }

    }
}