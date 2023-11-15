package top.bettercode.summer.tools.pay.weixinv3

import com.wechat.pay.java.core.RSAAutoCertificateConfig
import com.wechat.pay.java.core.cipher.PrivacyDecryptor
import com.wechat.pay.java.core.exception.ValidationException
import com.wechat.pay.java.core.http.AbstractHttpClient
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder
import com.wechat.pay.java.core.notification.NotificationParser
import com.wechat.pay.java.core.notification.RequestParam
import com.wechat.pay.java.core.util.PemUtil
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
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.Base64Utils
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.log.OkHttpClientLoggingInterceptor
import top.bettercode.summer.tools.pay.properties.WeixinV3PayProperties
import top.bettercode.summer.tools.pay.weixin.WeixinPayClient.Companion.LOG_MARKER
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.servlet.http.HttpServletRequest


/**
 * 微信支付v3接口
 *
 *  https://pay.weixin.qq.com/wiki/doc/apiv3/apis/index.shtml
 *
 * @author Peter Wu
 */
@Suppress("JoinDeclarationAndAssignment")
@LogMarker(LOG_MARKER)
open class WeixinV3PayClient(val properties: WeixinV3PayProperties) {

    private val log: Logger = LoggerFactory.getLogger(WeixinV3PayClient::class.java)

    companion object {
        const val LOG_MARKER = "weixinv3_pay"
    }

    val config: RSAAutoCertificateConfig
    val notificationParser: NotificationParser
    val httpClient: AbstractHttpClient
    private val publicKey: PublicKey
    private val privacyDecryptor: PrivacyDecryptor

    init {
        this.config = RSAAutoCertificateConfig.Builder()
                .merchantId(properties.merchantId)
                .privateKeyFromPath(properties.privateKeyPath)
                .merchantSerialNumber(properties.merchantSerialNumber)
                .apiV3Key(properties.apiV3Key)
                .build()

        this.privacyDecryptor = this.config.createDecryptor()

        // 解析公钥
        this.publicKey = PemUtil.loadX509FromStream(ClassPathResource(properties.certificatePath!!).inputStream).publicKey

        this.notificationParser = NotificationParser(config)

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(OkHttpClientLoggingInterceptor(collectionName = "第三方平台", name = "微信支付V3", logMarker = LOG_MARKER, logClazz = WeixinV3PayClient::class.java))
                .connectionPool(ConnectionPool(properties.maxIdleConnections, properties.keepAliveDuration, TimeUnit.SECONDS))
                .connectTimeout(properties.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(properties.readTimeout, TimeUnit.SECONDS)
                .build()
        this.httpClient = DefaultHttpClientBuilder().okHttpClient(okHttpClient).config(config).build()
    }

    /**
     * 应用ID
     */
    val appid: String? by lazy {
        this.properties.appid
    }

    /**
     * 商家转账
     *
     * https://pay.weixin.qq.com/docs/merchant/apis/batch-transfer-to-balance/transfer-batch/initiate-batch-transfer.html
     */
    val transferBatchService: TransferBatchService by lazy {
        TransferBatchService.Builder().httpClient(httpClient).encryptor(config.createEncryptor()).decryptor(config.createDecryptor()).build()

    }

    /**
     * APP支付
     * https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_2_1.shtml
     */
    val appService: AppService by lazy {
        AppService.Builder().httpClient(httpClient).build()
    }

    /**
     * H5支付
     * https://pay.weixin.qq.com/docs/merchant/apis/h5-payment/direct-jsons/h5-prepay.html
     */
    val h5Service: H5Service by lazy {
        H5Service.Builder().httpClient(httpClient).build()
    }

    /**
     * JSAPI支付
     * https://pay.weixin.qq.com/docs/merchant/apis/jsapi-payment/direct-jsons/jsapi-prepay.html
     */
    val jsapiService: JsapiService by lazy {
        JsapiService.Builder().httpClient(httpClient).build()
    }

    /**
     *Native支付
     * https://pay.weixin.qq.com/docs/merchant/apis/native-payment/direct-jsons/native-prepay.html
     */
    val nativePayService: NativePayService by lazy {
        NativePayService.Builder().httpClient(httpClient).build()
    }

    /**
     *退款
     * https://pay.weixin.qq.com/docs/merchant/apis/refund/refunds/create.html
     */
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
            ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("code" to "FAIL", "message" to "签名验证失败"))
        } catch (e: Exception) {
            // 如果处理失败，应返回 4xx/5xx 的状态码，例如 500 INTERNAL_SERVER_ERROR
            log.error("handle notify failed", e)
            ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("code" to "FAIL", "message" to e.message))
        }
    }

    fun publicEncrypt(data: String): String {
        return try {
            val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, this.publicKey)
            val cipherdata: ByteArray = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            Base64Utils.encodeToString(cipherdata)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("当前Java环境不支持RSA v1.5/OAEP", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("当前Java环境不支持RSA v1.5/OAEP", e)
        } catch (e: InvalidKeyException) {
            throw IllegalArgumentException("无效的证书", e)
        } catch (e: IllegalBlockSizeException) {
            throw IllegalBlockSizeException("加密原串的长度不能超过214字节")
        } catch (e: BadPaddingException) {
            throw IllegalBlockSizeException("加密原串的长度不能超过214字节")
        }
    }

    fun privacyDecrypt(data: String): String {
        return privacyDecryptor.decrypt(data)
    }
}