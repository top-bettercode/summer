@file:Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")

package top.bettercode.summer.tools.pay.weixinv3

import com.wechat.pay.java.core.RSAAutoCertificateConfig
import com.wechat.pay.java.core.exception.ValidationException
import com.wechat.pay.java.core.http.AbstractHttpClient
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder
import com.wechat.pay.java.core.notification.NotificationParser
import com.wechat.pay.java.core.notification.RequestParam
import com.wechat.pay.java.service.payments.app.AppService
import com.wechat.pay.java.service.payments.h5.H5Service
import com.wechat.pay.java.service.payments.jsapi.JsapiService
import com.wechat.pay.java.service.payments.nativepay.NativePayService
import com.wechat.pay.java.service.refund.RefundService
import com.wechat.pay.java.service.transferbatch.TransferBatchService
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.log.OkHttpClientLoggingInterceptor
import top.bettercode.summer.tools.pay.properties.WeixinV3PayProperties
import top.bettercode.summer.tools.pay.weixin.WeixinPayClient.Companion.LOG_MARKER
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.servlet.http.HttpServletRequest


/**
 * 微信支付v3接口
 *
 *  https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_2_1.shtml
 *
 *
 * @author Peter Wu
 */
@LogMarker(LOG_MARKER)
open class WeixinV3PayClient(val properties: WeixinV3PayProperties) {

    private val log: Logger = LoggerFactory.getLogger(WeixinV3PayClient::class.java)

    companion object {
        const val LOG_MARKER = "weixinv3_pay"
    }

    val config: RSAAutoCertificateConfig
    val notificationParser: NotificationParser
    val httpClient: AbstractHttpClient

    init {
        this.config = RSAAutoCertificateConfig.Builder()
                .merchantId(properties.merchantId)
                .privateKeyFromPath(properties.privateKeyPath)
                .merchantSerialNumber(properties.merchantSerialNumber)
                .apiV3Key(properties.apiV3Key)
                .build()
        this.notificationParser = NotificationParser(config)

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(OkHttpClientLoggingInterceptor("第三方平台", "微信支付V3"))
                .connectionPool(ConnectionPool(properties.maxIdleConnections, properties.keepAliveDuration, TimeUnit.SECONDS))
                .connectTimeout(properties.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(properties.readTimeout, TimeUnit.SECONDS)
                .build()
        this.httpClient = DefaultHttpClientBuilder().okHttpClient(okHttpClient).config(config).build()
    }

    val transferBatchService: TransferBatchService by lazy {
        TransferBatchService.Builder().httpClient(httpClient).encryptor(config.createEncryptor()).decryptor(config.createDecryptor()).build()

    }

    val appService: AppService by lazy {
        AppService.Builder().httpClient(httpClient).build()
    }

    val h5Service: H5Service by lazy {
        H5Service.Builder().httpClient(httpClient).build()
    }

    val jsapiService: JsapiService by lazy {
        JsapiService.Builder().httpClient(httpClient).build()
    }

    val nativePayService: NativePayService by lazy {
        NativePayService.Builder().httpClient(httpClient).build()
    }

    val refundService: RefundService by lazy {
        RefundService.Builder().httpClient(httpClient).build()
    }

    fun <T> handleNotify(request: HttpServletRequest, clazz: Class<T>, consumer: Consumer<T>): Any {
        val requestBody = request.reader.readText()
        val wechatpayNonce = request.getHeader("Wechatpay-Nonce")
        val wechatpayTimestamp = request.getHeader("Wechatpay-Timestamp")
        val wechatpaySerial = request.getHeader("Wechatpay-Serial")
        val wechatSignature = request.getHeader("Wechatpay-Signature")
        // 构造 RequestParam
        val requestParam = RequestParam.Builder()
                .serialNumber(wechatpaySerial)
                .nonce(wechatpayNonce)
                .signature(wechatSignature)
                .timestamp(wechatpayTimestamp)
                .body(requestBody)
                .build()
        return try {
            val response: T = notificationParser.parse(requestParam, clazz)
            consumer.accept(response)
            ResponseEntity.status(HttpStatus.OK)
        } catch (e: ValidationException) {
            // 签名验证失败，返回 401 UNAUTHORIZED 状态码
            log.error("sign verification failed", e)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        } catch (e: Exception) {
            // 如果处理失败，应返回 4xx/5xx 的状态码，例如 500 INTERNAL_SERVER_ERROR
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}