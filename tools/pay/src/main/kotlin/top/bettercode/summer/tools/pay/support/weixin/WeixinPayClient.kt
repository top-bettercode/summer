package top.bettercode.summer.tools.pay.support.weixin

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.util.DigestUtils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties
import top.bettercode.summer.tools.pay.support.WeixinPayException
import top.bettercode.summer.tools.pay.support.weixin.WeixinPayClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.pay.support.weixin.entity.UnifiedOrderRequest
import top.bettercode.summer.tools.pay.support.weixin.entity.UnifiedOrderResponse
import top.bettercode.summer.web.support.client.ApiTemplate
import java.util.*
import kotlin.reflect.full.memberProperties

/**
 * 公众号接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class WeixinPayClient(val properties: WeixinPayProperties) :
        ApiTemplate(
                "第三方平台",
                "微信支付",
                LOG_MARKER,
                properties.connectTimeout,
                properties.readTimeout
        ) {

    companion object {
        const val LOG_MARKER = "weixin_pay"
    }

    init {
        val messageConverter = object : MappingJackson2XmlHttpMessageConverter() {
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

    /**
     * 统一下单
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1
     */
    fun unifiedorder(request: UnifiedOrderRequest): UnifiedOrderResponse {
        request.appid = properties.appid
        request.mchId = properties.mchId
        request.sign = getSign(request)
        val entity = postForObject(
                "https://api.mch.weixin.qq.com/pay/unifiedorder",
                request,
                UnifiedOrderResponse::class.java
        )
        if (log.isDebugEnabled) {
            log.debug("查询结果：" + StringUtil.valueOf(entity))
        }
        return if (entity != null && getSign(entity) === entity.sign) {
            if (entity.isOk()) {
                if (entity.isBizOk()) {
                    entity
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}下单失败:${entity.errCodeDes}", entity)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}下单失败:${entity.returnMsg}", entity)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}下单失败:结果签名验证失败,${entity?.returnMsg}", entity)
        }
    }

    /**
     * 对参数签名
     *
     * @param params 参数
     * @return 签名后字符串
     */
    private fun getSign(params: Any): String {
        //获取待签名字符串
        val preStr = params::class.memberProperties.sortedBy { it.name }.map {
            val key = it.name
            val value = it.getter.call(params)
            if (value == null || value == "" || key.equals("sign", ignoreCase = true)) {
                null
            } else {
                "$key=$value"
            }
        }.filterNotNull().joinToString("&")
        val stringSignTemp = preStr + "&key=" + properties.apiKey
        return DigestUtils.md5DigestAsHex(stringSignTemp.toByteArray(charset("UTF-8"))).uppercase(Locale.getDefault())
    }

}