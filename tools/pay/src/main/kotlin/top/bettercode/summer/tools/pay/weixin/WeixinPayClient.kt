package top.bettercode.summer.tools.pay.weixin

import OrderQueryRequest
import OrderQueryResponse
import RefundQueryRequest
import RefundQueryResponse
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.util.DigestUtils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties
import top.bettercode.summer.tools.pay.weixin.WeixinPayClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.pay.weixin.entity.UnifiedOrderRequest
import top.bettercode.summer.tools.pay.weixin.entity.UnifiedOrderResponse
import top.bettercode.summer.web.support.client.ApiTemplate
import java.util.*
import kotlin.reflect.full.memberProperties


/**
 * 公众号接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class WeixinPayClient(val properties: WeixinPayProperties) : ApiTemplate(
        "第三方平台",
        "微信支付",
        LOG_MARKER,
        properties.connectTimeout,
        properties.readTimeout
) {
    companion object {
        const val LOG_MARKER = "weixin_pay"

        /**
         * 对参数签名
         *
         * @param params 参数
         * @return 签名后字符串
         */
        fun getSign(params: Any, apiKey: String): String {
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
            val stringSignTemp = preStr + "&key=" + apiKey
            return DigestUtils.md5DigestAsHex(stringSignTemp.toByteArray(charset("UTF-8"))).uppercase(Locale.getDefault())
        }


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
        if (request.notifyUrl == null) {
            request.notifyUrl = properties.notifyUrl
        }
        request.sign = getSign(request, properties.apiKey!!)
        val entity = postForObject(
                "https://api.mch.weixin.qq.com/pay/unifiedorder",
                request,
                UnifiedOrderResponse::class.java
        )
        if (log.isDebugEnabled) {
            log.debug("查询结果：" + StringUtil.valueOf(entity))
        }
        return if (entity != null && getSign(entity, properties.apiKey!!) === entity.sign) {
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
     * 查询订单
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_2&index=4
     */
    fun orderquery(request: OrderQueryRequest): OrderQueryResponse {
        request.appid = properties.appid
        request.mchId = properties.mchId
        request.sign = getSign(request, properties.apiKey!!)
        val entity = postForObject(
                "https://api.mch.weixin.qq.com/pay/orderquery",
                request,
                OrderQueryResponse::class.java
        )
        if (log.isDebugEnabled) {
            log.debug("查询结果：" + StringUtil.valueOf(entity))
        }
        return if (entity != null && getSign(entity, properties.apiKey!!) === entity.sign) {
            if (entity.isOk()) {
                if (entity.isBizOk()) {
                    entity
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}查询失败:${entity.errCodeDes},交易状态：${entity.tradeStateDesc}", entity)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}查询失败:${entity.returnMsg}", entity)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}查询失败:结果签名验证失败,${entity?.returnMsg}", entity)
        }

    }

    /**
     * 查询退款
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_5&index=7
     */
    fun refundquery(request: RefundQueryRequest): RefundQueryResponse {
        request.appid = properties.appid
        request.mchId = properties.mchId
        request.sign = getSign(request, properties.apiKey!!)
        val entity = postForObject(
                "https://api.mch.weixin.qq.com/pay/refundquery",
                request,
                RefundQueryResponse::class.java
                )
        if (log.isDebugEnabled) {
            log.debug("查询结果：" + StringUtil.valueOf(entity))
        }
        return if (entity != null && getSign(entity, properties.apiKey!!) === entity.sign) {
            if (entity.isOk()) {
                if (entity.isBizOk()) {
                    entity
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}查询退款:${entity.errCodeDes}", entity)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}查询退款:${entity.returnMsg}", entity)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}查询退款:结果签名验证失败,${entity?.returnMsg}", entity)
        }
    }

    /**
     *     #    付款到零钱（提现）
     *     withdraw_url: https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers
     *     #    查询付款信息
     *     transfer_info_url: https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo
     *
     */
}