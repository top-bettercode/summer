package top.bettercode.summer.tools.pay.weixin

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.lang.Nullable
import org.springframework.util.DigestUtils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.ApiTemplate
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties
import top.bettercode.summer.tools.pay.weixin.entity.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.*
import java.util.function.Consumer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.*
import javax.servlet.http.HttpServletRequest


/**
 * 微信支付接口
 *
 * @author Peter Wu
 */
@LogMarker(WeixinPayClient.MARKER)
open class WeixinPayClient(properties: WeixinPayProperties) : ApiTemplate<WeixinPayProperties>(
    marker = MARKER,
    properties = properties,
    requestFactory = if (properties.certLocation == null) SimpleClientHttpRequestFactory().apply {
        setConnectTimeout(properties.connectTimeout * 1000)
        setReadTimeout(properties.readTimeout * 1000)
    } else object : SimpleClientHttpRequestFactory() {
        override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
            //指定读取证书格式为PKCS12
            val keyStore = KeyStore.getInstance("PKCS12")
            val certFileResource = ClassPathResource(properties.certLocation!!)
            val certInputStream: InputStream = certFileResource.inputStream
            val certStorePassword = (properties.certStorePassword
                ?: throw IllegalArgumentException("certStorePassword is null"))
            keyStore.load(certInputStream, certStorePassword.toCharArray())

            val keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            val certKeyPassword = (properties.certKeyPassword
                ?: throw IllegalArgumentException("certKeyPassword is null"))
            keyManagerFactory.init(keyStore, certKeyPassword.toCharArray())

            val sslContext = SSLContext.getInstance("TLS")

            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return emptyArray()
                }
            }
            sslContext.init(
                keyManagerFactory.keyManagers,
                arrayOf(trustManager),
                java.security.SecureRandom()
            )
            val httpsConnection = connection as HttpsURLConnection
            httpsConnection.sslSocketFactory = sslContext.socketFactory

            httpsConnection.setHostnameVerifier { _: String?, _: SSLSession? -> true }

            super.prepareConnection(connection, httpMethod)
        }
    }.apply {
        setConnectTimeout(properties.connectTimeout * 1000)
        setReadTimeout(properties.readTimeout * 1000)
    }
) {
    companion object {
        const val MARKER = "weixin_pay"
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
        this.messageConverters = messageConverters
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
        if (log.isDebugEnabled)
            log.debug("等处理的字符中：$stringSignTemp")
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
    open fun unifiedorder(request: UnifiedOrderRequest): UnifiedOrderResponse {
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
                    throw clientException(
                        "订单：${request.outTradeNo}下单失败:${response.errCodeDes}",
                        response
                    )
                }
            } else {
                throw clientException(
                    "订单：${request.outTradeNo}下单失败:结果签名验证失败,${response.returnMsg}",
                    response
                )
            }
        } else {
            throw clientException(
                "订单：${request.outTradeNo}下单失败:${response?.returnMsg ?: "无结果响应"}",
                response
            )
        }
    }

    /**
     * jsapi 调起支付接口 支付信息
     * https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6
     *
     */
    open fun getJsapiWCPayRequest(unifiedOrderResponse: UnifiedOrderResponse): JsapiWCPayRequest {
        val jsapiWCPayRequest = JsapiWCPayRequest(
            appId = unifiedOrderResponse.appid!!,
            `package` = "prepay_id=${unifiedOrderResponse.prepayId}",
        )
        jsapiWCPayRequest.paySign = sign(jsapiWCPayRequest).sign
        return jsapiWCPayRequest
    }

    /**
     * app 调起支付接口 支付信息
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12&index=2
     */
    open fun getAppWCPayRequest(unifiedOrderResponse: UnifiedOrderResponse): AppWCPayRequest {
        val appWCPayRequest = AppWCPayRequest(
            appid = unifiedOrderResponse.appid!!,
            partnerid = unifiedOrderResponse.mchId!!,
            prepayid = unifiedOrderResponse.prepayId
        )
        appWCPayRequest.sign = sign(appWCPayRequest).sign
        return appWCPayRequest
    }

    /**
     * 查询订单
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_2&index=4
     */
    open fun orderquery(request: OrderQueryRequest): OrderQueryResponse {
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
                    throw clientException(
                        "订单：${request.outTradeNo}查询失败:${response.errCodeDes},交易状态：${response.tradeStateDesc}",
                        response
                    )
                }
            } else {
                throw clientException(
                    "订单：${request.outTradeNo}查询失败:结果签名验证失败,${response.returnMsg}",
                    response
                )
            }
        } else {
            throw clientException(
                "订单：${request.outTradeNo}查询失败:${response?.returnMsg ?: "无结果响应"}",
                response
            )
        }

    }

    /**
     * 查询退款
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_5&index=7
     */
    open fun refundquery(request: RefundQueryRequest): RefundQueryResponse {
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
                    throw clientException(
                        "订单：${request.outTradeNo}查询退款:${response.errCodeDes}",
                        response
                    )
                }
            } else {
                throw clientException(
                    "订单：${request.outTradeNo}查询退款:结果签名验证失败,${response.returnMsg}",
                    response
                )
            }
        } else {
            throw clientException(
                "订单：${request.outTradeNo}查询退款:${response?.returnMsg ?: "无结果响应"}",
                response
            )
        }
    }

    /**
     * 支付结果通知处理
     * 示例：
     * <pre>
     * <?xml version="1.0" encoding="UTF-8"?>
     * <xml>
     * <appid><![CDATA[wx7edf5a744ece817f]]></appid>
     * <bank_type><![CDATA[OTHERS]]></bank_type>
     * <cash_fee><![CDATA[2]]></cash_fee>
     * <fee_type><![CDATA[CNY]]></fee_type>
     * <is_subscribe><![CDATA[N]]></is_subscribe>
     * <mch_id><![CDATA[1608025116]]></mch_id>
     * <nonce_str><![CDATA[8vvOOwkeV8hCRC5xLzG4Ok24QRv246t8]]></nonce_str>
     * <openid><![CDATA[okQsW5ZlTXXYZ2HKrAnHfeXJLkeA]]></openid>
     * <out_trade_no><![CDATA[WX2023111317239400003]]></out_trade_no>
     * <result_code><![CDATA[SUCCESS]]></result_code>
     * <return_code><![CDATA[SUCCESS]]></return_code>
     * <sign><![CDATA[42145C8D61A1DB8DFEC66954B78719F1]]></sign>
     * <time_end><![CDATA[20231113134646]]></time_end>
     * <total_fee>2</total_fee>
     * <trade_type><![CDATA[JSAPI]]></trade_type>
     * <transaction_id><![CDATA[4200001998202311135246700447]]></transaction_id>
     * </xml>
     * </pre>
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_7&index=3
     */
    @JvmOverloads
    fun handleNotify(
        request: HttpServletRequest,
        success: (PayResponse) -> WeixinPayResponse,
        fail: ((PayResponse) -> WeixinPayResponse)? = null,
        error: Consumer<Exception> = Consumer<Exception> {
            log.error(
                "支付结果通知处理失败",
                it
            )
        }
    ): Any {
        try {
            val response = objectMapper.readValue(request.inputStream, PayResponse::class.java)
            if (response != null && response.isOk()) {
                if (sign(response).sign == response.sign) {
                    return if (response.isBizOk()) {
                        if (properties.mchId == response.mchId && properties.appid == response.appid) {
                            success(response)
                        } else {
                            throw clientException(
                                "微信支付异步通知失败，商户/应用不匹配,响应商户：${response.mchId},本地商户：${properties.mchId},响应应用ID：${response.appid},本地应用ID：${properties.appid}",
                                response
                            )
                        }
                    } else {
                        if (fail == null) {
                            throw clientException(
                                "订单：${response.outTradeNo}支付失败:${response.errCodeDes}",
                                response
                            )
                        } else {
                            log.error("订单：${response.outTradeNo}支付失败:${response.errCodeDes}")
                            fail(response)
                        }
                    }
                } else {
                    throw clientException(
                        "订单：${response.outTradeNo}支付失败:结果签名验证失败,${response.returnMsg}",
                        response
                    )
                }
            } else {
                throw clientException(
                    "订单：${response.outTradeNo}支付失败:${response?.returnMsg ?: "无结果响应"}",
                    response
                )
            }
        } catch (e: Exception) {
            error.accept(e)
        }
        return WeixinPayResponse.fail()
    }

    /**
     * 退款结果通知数据处理
     * 示例：
     * <pre>
     * <?xml version="1.0" encoding="UTF-8"?>
     * <xml>
     * <return_code>SUCCESS</return_code>
     * <appid><![CDATA[wx7edf5a744ece817f]]></appid>
     * <mch_id><![CDATA[1608025116]]></mch_id>
     * <nonce_str><![CDATA[755c63dbd15c19787398e6a062bdef98]]></nonce_str>
     * <req_info><![CDATA[WXj7zEJw4dH9qBCY0KUTQNTwdQVKWzrs60mbLKCs/QAUZBLs5Sv+EFgZr61iunt9Qv4I8sSfF9JjFhQZ2wp7uqE6w4GvyDTP95RpsOdjzcrFB9aFX+c1NEYrZgJQKi7N7YU1HLuj4cPPox50srU5eiZRH4BSEpN/R7CeItZUm4TRD+55XLunmVYmkWsFvR9HoUssC06W46Y93V8kn2o1rKs2jtuwc6PK9zTQpr+HdRSzU/FWIDAtVV6NqkUTWFUKHvG8WQgvfexZzh1ta+yT0W21RGfqGF00aRBiyvuihpfO/hXP79kuT0XVCgsYl7eUdDdIcVX7Xnyr9uVDUuyal+6PfVIUglsDuVHClS1/PtiGeBhid8FUtyvhzInMwqU9fcDybOObmRcizI2WWQpk7HkJleXy2JIA9RMaPSsVgjyWDIGOajqE0NmYMmAgqal7MHOatu0pTdayf2blErPzyeNNlp1W+4CPvTmakGB8MeQu5I3PtRV9/5dNwl6uQsEyVF6d3rFxgH+k41uPlzUITdZNoW72scaR/Rlh8EXUhd7PjJTdu0I6ZX5YcVH0cZMHY8BW/C5DXv1VwI37DjrdWFmbL7ncr1njZFEY6QpuzJ9GXjHiDmg+hcxh1NhUFYVka58qROcO1SSu49bHGN29auHvaLX8dHDBe+TOVr3c/ohqIPa2x+oQ4wPNN1MElkbD2qvPmw9SgeLIhegz7bVcy6B5A7/CGfTlc9HlMmr639b0qbPctiqiRSnPP105jix9+0dDIe/psJ8Lb92zIcYH1z1LMJCBOSjdGNS1bAkeKieQAJKnzlBUf6W6/xnqou+WoxoK6InDC3k97sIifigt8udvk+l9haEnTnCO9rgQawuaaK9AqtY9ESt4JkE4JRfiYt64lGJybnhL+B0l14J6+tpGEvicVaNtk4wMNHQQW4f79g+EcyV+3d7d17K6Rjyqu3MJYChJVZW7OYeoiWIm2qIzsEL7Iel/Ts1FEGs/jQWSSBMTylt6MKIOP6zZKB6glW/MFFbJw4Y8XnJMLWFNeEHWgPz99SZbftSmdk4FHxkdqvY2/W2r4vLskW0rNkenPdX7LuQAKfxoIQT6knEFnZAXjKMIpUMlOQ35vdderEWGk3p3yY2xczjHZf2j3nEc]]></req_info>
     * </xml>
     * </pre>
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_16&index=11
     */
    @JvmOverloads
    fun handleRefundNotify(
        request: HttpServletRequest,
        success: (RefundInfo, RefundNotifyResponse) -> WeixinPayResponse,
        error: Consumer<Exception> = Consumer<Exception> {
            log.error(
                "退款结果通知处理失败",
                it
            )
        }
    ): Any {
        try {
            val response =
                objectMapper.readValue(request.inputStream, RefundNotifyResponse::class.java)
            if (response != null && response.isOk()) {
                val refundInfo = decryptRefundInfo(response.reqInfo!!)
                return success(refundInfo, response)
            } else {
                throw clientException(
                    "退款结果通知失败:${response?.returnMsg ?: "无结果响应"}",
                    response
                )
            }
        } catch (e: Exception) {
            error.accept(e)
        }
        return WeixinPayResponse.fail()
    }

    private fun decryptRefundInfo(encryptedData: String): RefundInfo {
        // Step 1: Base64 decode the encrypted data
        val encryptedBytes = Base64.getDecoder().decode(encryptedData)

        // Step 2: Generate MD5 hash of the merchant key
        val keyBytes = DigestUtils.md5DigestAsHex(properties.apiKey!!.toByteArray(charset("UTF-8")))

        // Step 3: Perform AES-256-ECB decryption
        val keySpec = SecretKeySpec(keyBytes.toByteArray(), "AES")
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
    open fun refund(request: RefundRequest): RefundResponse {
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
                    throw clientException(
                        "订单：${request.outTradeNo}退款失败:${response.errCodeDes}",
                        response
                    )
                }
            } else {
                throw clientException(
                    "订单：${request.outTradeNo}退款失败:结果签名验证失败,${response.returnMsg}",
                    response
                )
            }
        } else {
            throw clientException(
                "订单：${request.outTradeNo}退款失败:${response?.returnMsg ?: "无结果响应"}",
                response
            )
        }

    }

    /**
     * 关闭订单
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_3&index=5
     */
    open fun closeOrder(request: CloseOrderRequest): CloseOrderResponse {
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
                    throw clientException(
                        "订单：${request.outTradeNo}关闭失败:${response.errCodeDes}",
                        response
                    )
                }
            } else {
                throw clientException(
                    "订单：${request.outTradeNo}关闭失败:结果签名验证失败,${response.returnMsg}",
                    response
                )
            }
        } else {
            throw clientException(
                "订单：${request.outTradeNo}关闭失败:${response?.returnMsg ?: "无结果响应"}",
                response
            )
        }

    }

    /**
     * 付款
     * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_2
     */
    open fun transfers(request: TransfersRequest): TransfersResponse {
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
                throw clientException(
                    "订单：${request.partnerTradeNo}付款失败:${response.errCodeDes}",
                    response
                )
            }
        } else {
            throw clientException(
                "订单：${request.partnerTradeNo}付款失败:${response?.returnMsg ?: "无结果响应"}",
                response
            )
        }

    }

    /**
     * 查询付款
     * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_3
     */
    open fun getTransferInfo(request: TransferInfoRequest): TransferInfoResponse {
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
                throw clientException(
                    "订单：${request.partnerTradeNo}查询付款失败:${response.errCodeDes}",
                    response
                )
            }
        } else {
            throw clientException(
                "订单：${request.partnerTradeNo}查询付款失败:${response?.returnMsg ?: "无结果响应"}",
                response
            )
        }
    }

}