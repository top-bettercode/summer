@file:Suppress("unused")

package top.bettercode.summer.tools.pay.weixin

import OrderQueryRequest
import OrderQueryResponse
import PayNotifyResponse
import RefundInfo
import RefundNotifyResponse
import RefundQueryRequest
import RefundQueryResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.util.DigestUtils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties
import top.bettercode.summer.tools.pay.weixin.WeixinPayClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.pay.weixin.response.UnifiedOrderRequest
import top.bettercode.summer.tools.pay.weixin.response.UnifiedOrderResponse
import top.bettercode.summer.web.support.client.ApiTemplate
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletRequest
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

    val objectMapper: ObjectMapper

    init {
        val messageConverter = object : MappingJackson2XmlHttpMessageConverter() {
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
        val response = postForObject(
                "https://api.mch.weixin.qq.com/pay/unifiedorder",
                request,
                UnifiedOrderResponse::class.java
        )
        if (log.isDebugEnabled) {
            log.debug("查询结果：" + StringUtil.valueOf(response))
        }
        return if (response != null && getSign(response, properties.apiKey!!) === response.sign) {
            if (response.isOk()) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}下单失败:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}下单失败:${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}下单失败:结果签名验证失败,${response?.returnMsg}", response)
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
        val response = postForObject(
                "https://api.mch.weixin.qq.com/pay/orderquery",
                request,
                OrderQueryResponse::class.java
        )
        if (log.isDebugEnabled) {
            log.debug("查询结果：" + StringUtil.valueOf(response))
        }
        return if (response != null && getSign(response, properties.apiKey!!) === response.sign) {
            if (response.isOk()) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}查询失败:${response.errCodeDes},交易状态：${response.tradeStateDesc}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}查询失败:${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}查询失败:结果签名验证失败,${response?.returnMsg}", response)
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
        val response = postForObject(
                "https://api.mch.weixin.qq.com/pay/refundquery",
                request,
                RefundQueryResponse::class.java
        )
        if (log.isDebugEnabled) {
            log.debug("查询结果：" + StringUtil.valueOf(response))
        }
        return if (response != null && getSign(response, properties.apiKey!!) === response.sign) {
            if (response.isOk()) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}查询退款:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}查询退款:${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}查询退款:结果签名验证失败,${response?.returnMsg}", response)
        }
    }

    /**
     * 支付结果通知处理
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_7&index=3
     */
    fun handleNotify(request: HttpServletRequest, consumer: Consumer<PayNotifyResponse>) {
        val response = objectMapper.readValue(request.inputStream, PayNotifyResponse::class.java)
        if (response != null && getSign(response, properties.apiKey!!) === response.sign) {
            if (response.isOk()) {
                if (response.isBizOk()) {
                    if (properties.mchId == response.mchId && properties.appid == response.appid) {
                        consumer.accept(response)
                    } else {
                        throw WeixinPayException("微信支付异步通知失败，商户/应用不匹配,响应商户：${response.mchId},本地商户：${properties.mchId},响应应用ID：${response.appid},本地应用ID：${properties.appid}", response)
                    }
                } else {
                    throw WeixinPayException("订单：${response.outTradeNo}支付失败:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${response.outTradeNo}支付失败:${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${response.outTradeNo}支付失败:结果签名验证失败,${response?.returnMsg}", response)
        }
    }

    /**
     * 退款结果通知数据处理
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_16&index=11
     */
    fun handleRefundNotify(request: HttpServletRequest, consumer: BiConsumer<RefundInfo, RefundNotifyResponse>) {
        val response = objectMapper.readValue(request.inputStream, RefundNotifyResponse::class.java)
        if (response.isOk()) {
            val refundInfo = decryptRefundInfo(response.reqInfo!!)
            consumer.accept(refundInfo, response)
        } else {
            throw WeixinPayException("退款结果通知失败:${response.returnMsg}", response)
        }
    }

    private fun decryptRefundInfo(encryptedData: String): RefundInfo {
        // Step 1: Base64 decode the encrypted data
        val encryptedBytes = Base64.getDecoder().decode(encryptedData)

        // Step 2: Generate MD5 hash of the merchant key
        val keyBytes = DigestUtils.md5Digest(properties.apiKey!!.toByteArray(charset("UTF-8")))

        // Step 3: Perform AES-256-ECB decryption
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return objectMapper.readValue(decryptedBytes, RefundInfo::class.java)
    }

    /**
     *     #    付款到零钱（提现）
     *     withdraw_url: https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers
     *     #    查询付款信息
     *     transfer_info_url: https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo
     *
     */
}