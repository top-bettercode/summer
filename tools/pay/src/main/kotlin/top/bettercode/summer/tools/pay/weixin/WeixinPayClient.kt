package top.bettercode.summer.tools.pay.weixin

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.util.DigestUtils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.RandomUtil
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties
import top.bettercode.summer.tools.pay.weixin.WeixinPayClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.pay.weixin.entity.*
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.support.client.ApiTemplate
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import javax.servlet.http.HttpServletRequest


/**
 * 微信支付接口
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
    }

    private val objectMapper: ObjectMapper

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

        //添加证书
        val certLocation = properties.certLocation
        if (certLocation != null) {
            //指定读取证书格式为PKCS12
            val keyStore = KeyStore.getInstance("PKCS12")
            val certFileResource = ClassPathResource(certLocation)
            val certInputStream: InputStream = certFileResource.inputStream
            val certStorePassword = (properties.certStorePassword
                    ?: throw IllegalArgumentException("certStorePassword is null"))
            keyStore.load(certInputStream, certStorePassword.toCharArray())

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            val certKeyPassword = (properties.certKeyPassword
                    ?: throw IllegalArgumentException("certKeyPassword is null"))
            keyManagerFactory.init(keyStore, certKeyPassword.toCharArray())

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagerFactory.keyManagers, null, null)
            sslContext.socketFactory

            val okHttpClient = OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory, object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                        }

                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return emptyArray()
                        }
                    })
                    .connectTimeout(properties.connectTimeout.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
                    .readTimeout(properties.readTimeout.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
                    .build()
            val okHttp3ClientHttpRequestFactory = OkHttp3ClientHttpRequestFactory(okHttpClient)
            super.setRequestFactory(okHttp3ClientHttpRequestFactory)
        }
    }

    /**
     * 对参数签名
     *  https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=4_3
     * @param source 参数
     * @return 签名后字符串
     */
    private fun sign(source: Any): SignMap {
        //获取待签名字符串
        val map: SignMap = this.objectMapper.convertValue(source, SignMap::class.java)

        val preStr = map.keys.sorted().mapNotNull {
            val key = it
            val value = map[key]
            if (value == null || value == "" || key.equals("sign", ignoreCase = true)) {
                null
            } else {
                "$key=$value"
            }
        }.joinToString("&")
        val stringSignTemp = "$preStr&key=${this.properties.apiKey}"
        if (IFormkeyService.log.isDebugEnabled)
            IFormkeyService.log.debug("等处理的字符中：$stringSignTemp")
        val sign = DigestUtils.md5DigestAsHex(stringSignTemp.toByteArray()).uppercase()
        map.sign = sign
        return map
    }

    fun writeToXml(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    /**
     * 统一下单
     * https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_1
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1
     */
    fun unifiedorder(request: UnifiedOrderRequest): UnifiedOrderResponse {
        request.mchId = properties.mchId
        if (request.appid == null)
            request.appid = properties.appid
        if (request.notifyUrl == null) {
            request.notifyUrl = properties.notifyUrl
        }
        val response = postForObject(
                "https://api.mch.weixin.qq.com/pay/unifiedorder",
                sign(request),
                UnifiedOrderResponse::class.java
        )
        return if (response != null && response.isOk()) {
            if (sign(response).sign == response.sign) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}下单失败:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}下单失败:结果签名验证失败,${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}下单失败:${response?.returnMsg ?: "无结果响应"}", response)
        }
    }

    /**
     * jsapi 调起支付接口 支付信息
     * https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6
     *
     * app
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12&index=2
     */
    fun getBrandWCPayRequest(unifiedOrderResponse: UnifiedOrderResponse): BrandWCPayRequest {
        val brandWCPayRequest = BrandWCPayRequest(appId = unifiedOrderResponse.appid,
                timeStamp = (System.currentTimeMillis() / 1000).toString(),
                nonceStr = RandomUtil.nextString2(32),
                `package` = "prepay_id=${unifiedOrderResponse.prepayId}",
                signType = "MD5")
        brandWCPayRequest.paySign = sign(brandWCPayRequest).sign
        return brandWCPayRequest
    }

    /**
     * 查询订单
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_2&index=4
     */
    fun orderquery(request: OrderQueryRequest): OrderQueryResponse {
        request.mchId = properties.mchId
        if (request.appid == null)
            request.appid = properties.appid
        val response = postForObject(
                "https://api.mch.weixin.qq.com/pay/orderquery",
                sign(request),
                OrderQueryResponse::class.java
        )
        return if (response != null && response.isOk()) {
            if (sign(response).sign == response.sign) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}查询失败:${response.errCodeDes},交易状态：${response.tradeStateDesc}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}查询失败:结果签名验证失败,${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}查询失败:${response?.returnMsg ?: "无结果响应"}", response)
        }

    }

    /**
     * 查询退款
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_5&index=7
     */
    fun refundquery(request: RefundQueryRequest): RefundQueryResponse {
        request.mchId = properties.mchId
        if (request.appid == null)
            request.appid = properties.appid
        val response = postForObject(
                "https://api.mch.weixin.qq.com/pay/refundquery",
                sign(request),
                RefundQueryResponse::class.java
        )
        return if (response != null && response.isOk()) {
            if (sign(response).sign == response.sign) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}查询退款:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}查询退款:结果签名验证失败,${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}查询退款:${response?.returnMsg ?: "无结果响应"}", response)
        }
    }

    /**
     * 支付结果通知处理
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_7&index=3
     */
    @JvmOverloads
    fun handleNotify(request: HttpServletRequest, success: (PayResponse) -> WeixinPayResponse, fail: ((PayResponse) -> WeixinPayResponse)? = null): Any {
        try {
            val response = objectMapper.readValue(request.inputStream, PayResponse::class.java)
            if (response != null && response.isOk()) {
                if (sign(response).sign == response.sign) {
                    return if (response.isBizOk()) {
                        if (properties.mchId == response.mchId && properties.appid == response.appid) {
                            success(response)
                        } else {
                            throw WeixinPayException("微信支付异步通知失败，商户/应用不匹配,响应商户：${response.mchId},本地商户：${properties.mchId},响应应用ID：${response.appid},本地应用ID：${properties.appid}", response)
                        }
                    } else {
                        if (fail == null) {
                            throw WeixinPayException("订单：${response.outTradeNo}支付失败:${response.errCodeDes}", response)
                        } else {
                            log.error("订单：${response.outTradeNo}支付失败:${response.errCodeDes}")
                            fail(response)
                        }
                    }
                } else {
                    throw WeixinPayException("订单：${response.outTradeNo}支付失败:结果签名验证失败,${response.returnMsg}", response)
                }
            } else {
                throw WeixinPayException("订单：${response.outTradeNo}支付失败:${response?.returnMsg ?: "无结果响应"}", response)
            }
        } catch (e: Exception) {
            log.error("退款结果通知失败", e)
        }
        return WeixinPayResponse.fail()
    }

    /**
     * 退款结果通知数据处理
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_16&index=11
     */
    fun handleRefundNotify(request: HttpServletRequest, success: (RefundInfo, RefundNotifyResponse) -> WeixinPayResponse): Any {
        try {
            val response = objectMapper.readValue(request.inputStream, RefundNotifyResponse::class.java)
            if (response != null && response.isOk()) {
                val refundInfo = decryptRefundInfo(response.reqInfo!!)
                return success(refundInfo, response)
            } else {
                throw WeixinPayException("退款结果通知失败:${response?.returnMsg ?: "无结果响应"}", response)
            }
        } catch (e: Exception) {
            log.error("退款结果通知失败", e)
        }
        return WeixinPayResponse.fail()
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
     * 申请退款
     *
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_4&index=6
     */
    fun refund(request: RefundRequest): RefundResponse {
        request.mchId = properties.mchId
        if (request.appid == null)
            request.appid = properties.appid
        if (request.notifyUrl == null) {
            request.notifyUrl = properties.notifyUrl
        }
        val response = postForObject(
                "https://api.mch.weixin.qq.com/secapi/pay/refund",
                sign(request),
                RefundResponse::class.java
        )
        return if (response != null && response.isOk()) {
            if (sign(response).sign == response.sign) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}退款失败:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}退款失败:结果签名验证失败,${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}退款失败:${response?.returnMsg ?: "无结果响应"}", response)
        }

    }

    /**
     * 关闭订单
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_3&index=5
     */
    fun closeOrder(request: CloseOrderRequest): CloseOrderResponse {
        request.mchId = properties.mchId
        if (request.appid == null)
            request.appid = properties.appid
        val response = postForObject(
                "https://api.mch.weixin.qq.com/pay/closeorder",
                sign(request),
                CloseOrderResponse::class.java
        )
        return if (response != null && response.isOk()) {
            if (sign(response).sign == response.sign) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}关闭失败:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}关闭失败:结果签名验证失败,${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}关闭失败:${response?.returnMsg ?: "无结果响应"}", response)
        }

    }

    /**
     * 付款
     * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_2
     */
    fun transfers(request: TransfersRequest): TransfersResponse {
        request.mchid = properties.mchId
        if (request.mchAppid == null)
            request.mchAppid = properties.appid
        val response = postForObject(
                "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers",
                sign(request),
                TransfersResponse::class.java
        )
        return if (response != null && response.isOk()) {
            if (response.isBizOk()) {
                response
            } else {
                throw WeixinPayException("订单：${request.partnerTradeNo}付款失败:${response.errCodeDes}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.partnerTradeNo}付款失败:${response?.returnMsg ?: "无结果响应"}", response)
        }

    }

    /**
     * 查询付款
     * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_3
     */
    fun getTransferInfo(request: TransferInfoRequest): TransferInfoResponse {
        request.mchId = properties.mchId
        if (request.appid == null)
            request.appid = properties.appid
        val response = postForObject(
                "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo",
                sign(request),
                TransferInfoResponse::class.java
        )
        return if (response != null && response.isOk()) {
            if (response.isBizOk()) {
                response
            } else {
                throw WeixinPayException("订单：${request.partnerTradeNo}查询付款失败:${response.errCodeDes}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.partnerTradeNo}查询付款失败:${response?.returnMsg ?: "无结果响应"}", response)
        }
    }

}