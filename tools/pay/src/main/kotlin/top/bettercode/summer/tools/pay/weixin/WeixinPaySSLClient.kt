package top.bettercode.summer.tools.pay.weixin

import com.fasterxml.jackson.annotation.JsonInclude
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.lang.Nullable
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.pay.properties.WeixinPayProperties
import top.bettercode.summer.tools.pay.weixin.WeixinPayClient.Companion.LOG_MARKER
import top.bettercode.summer.tools.pay.weixin.entity.RefundRequest
import top.bettercode.summer.tools.pay.weixin.entity.RefundResponse
import top.bettercode.summer.web.support.client.ApiTemplate
import java.io.File
import javax.net.ssl.SSLContext


/**
 * 公众号接口
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
        // 读取证书文件

        //指定读取证书格式为PKCS12
        val certFile = File(properties.certPath
                ?: throw IllegalArgumentException("certPath is null"))

        // 设置证书信息
        val sslContext: SSLContext = SSLContexts.custom().loadKeyMaterial(certFile, (properties.certStorePassword
                ?: throw IllegalArgumentException("certStorePassword is null")).toCharArray(), (properties.certKeyPassword
                ?: throw IllegalArgumentException("certKeyPassword is null")).toCharArray()).build()
        val socketFactory = SSLConnectionSocketFactory(sslContext)

        // 创建HttpClient并设置证书
        val httpClient: CloseableHttpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build()
        val sslRequestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        super.setRequestFactory(sslRequestFactory)
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
        val entity = postForObject(
                "https://api.mch.weixin.qq.com/pay/unifiedorder",
                request,
                RefundResponse::class.java
        )
        if (log.isDebugEnabled) {
            log.debug("查询结果：" + StringUtil.valueOf(entity))
        }
        return if (entity != null && WeixinPayClient.getSign(entity, properties.apiKey!!) === entity.sign) {
            if (entity.isOk()) {
                if (entity.isBizOk()) {
                    entity
                } else {
                    throw WeixinPayException("订单：${request.outTradeNo}退款失败:${entity.errCodeDes}", entity)
                }
            } else {
                throw WeixinPayException("订单：${request.outTradeNo}退款失败:${entity.returnMsg}", entity)
            }
        } else {
            throw WeixinPayException("订单：${request.outTradeNo}退款失败:结果签名验证失败,${entity?.returnMsg}", entity)
        }

    }

    /**
     *     order_query: https://api.mch.weixin.qq.com/pay/orderquery
     *     refund_query_url: https://api.mch.weixin.qq.com/pay/refundquery
     *     #    付款到零钱（提现）
     *     withdraw_url: https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers
     *     #    查询付款信息
     *     transfer_info_url: https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo
     *
     */

}