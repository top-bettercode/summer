@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package top.bettercode.summer.tools.pay.weixin

import com.fasterxml.jackson.annotation.JsonInclude
import okhttp3.OkHttpClient
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.http.MediaType
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.lang.Nullable
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties
import top.bettercode.summer.tools.pay.weixin.WeixinPayClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.pay.weixin.entity.*
import top.bettercode.summer.web.support.client.ApiTemplate
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


/**
 * 微信支付接口
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class WeixinPaySSLClient(val properties: WeixinPayProperties) : ApiTemplate(
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

        //添加证书
        //指定读取证书格式为PKCS12
        val patternResolver = PathMatchingResourcePatternResolver()
        val keyStore = KeyStore.getInstance("PKCS12")
        val certFileResource = patternResolver.getResource(properties.certLocation
                ?: throw IllegalArgumentException("certPath is null"))
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

    /**
     * 申请退款
     *
     * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_4&index=6
     */
    fun refund(request: RefundRequest): RefundResponse {
        request.appid = properties.appid
        request.mchId = properties.mchId
        if (request.notifyUrl == null) {
            request.notifyUrl = properties.notifyUrl
        }
        request.sign = WeixinPayClient.getSign(request, properties.apiKey!!)
        val response = postForObject(
                "https://api.mch.weixin.qq.com/secapi/pay/refund",
                request,
                RefundResponse::class.java
        )
        return if (response != null && WeixinPayClient.getSign(response, properties.apiKey!!) === response.sign) {
            if (response.isOk()) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}退款失败:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}退款失败:${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}退款失败:结果签名验证失败,${response?.returnMsg}", response)
        }

    }

    /**
     * 付款
     * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_2
     */
    fun transfers(request: TransfersRequest): TransfersResponse {
        request.mchAppid = properties.appid
        request.mchid = properties.mchId
        request.sign = WeixinPayClient.getSign(request, properties.apiKey!!)
        val response = postForObject(
                "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers",
                request,
                TransfersResponse::class.java
        )
        return if (response != null) {
            if (response.isOk()) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.partnerTradeNo}付款失败:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.partnerTradeNo}付款失败:${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.partnerTradeNo}付款失败:无结果响应")
        }
    }

    /**
     * 查询付款
     * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_3
     */
    fun getTransferInfo(request: TransferInfoRequest): TransferInfoResponse {
        request.appid = properties.appid
        request.mchId = properties.mchId
        request.sign = WeixinPayClient.getSign(request, properties.apiKey!!)
        val response = postForObject(
                "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo",
                request,
                TransferInfoResponse::class.java
        )
        return if (response != null) {
            if (response.isOk()) {
                if (response.isBizOk()) {
                    response
                } else {
                    throw WeixinPayException("订单：${request.partnerTradeNo}查询付款失败:${response.errCodeDes}", response)
                }
            } else {
                throw WeixinPayException("订单：${request.partnerTradeNo}查询付款失败:${response.returnMsg}", response)
            }
        } else {
            throw WeixinPayException("订单：${request.partnerTradeNo}查询付款失败:无结果响应")
        }
    }

}