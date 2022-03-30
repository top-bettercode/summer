package top.bettercode.sms.aliyun

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.util.Base64Utils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.DefaultResponseErrorHandler
import top.bettercode.lang.util.StringUtil.json
import top.bettercode.sms.SmsException
import top.bettercode.sms.SmsSysException
import top.bettercode.sms.SmsTemplate
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 阿里短信平台 接口请求
 */
class AliSmsTemplate(
    private val aliSmsProperties: AliSmsProperties
) : SmsTemplate(
    "第三方接口", "阿里短信平台", LOG_MARKER_STR, aliSmsProperties.connectTimeout,
    aliSmsProperties.readTimeout, null
) {

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
            object : MappingJackson2HttpMessageConverter() {
                override fun canRead(mediaType: MediaType?): Boolean {
                    return true
                }

                override fun canWrite(clazz: Class<*>?, mediaType: MediaType?): Boolean {
                    return true
                }
            }
        val objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(AllEncompassingFormHttpMessageConverter())
        messageConverters.add(messageConverter)
        setMessageConverters(messageConverters)

        errorHandler = object : DefaultResponseErrorHandler() {
            override fun handleError(response: ClientHttpResponse) {}
        }
    }

    /**
     * 发送短信。
     *
     * @param templateCode 短信模板CODE。
     * @param phoneNumber 接收短信的手机号码
     * @param signName 短信签名名称
     * @param templateParam 短信模板变量对应的实际值
     * @return 结果
     */
    fun sendSms(
        templateCode: String, phoneNumber: String, signName: String,
        templateParam: Map<String, String>
    ): AliSmsResponse {
        return sendSms(templateCode, listOf(AliSmsReq(phoneNumber, signName, templateParam)))
    }

    /**
     * 发送短信。
     *
     * @param templateCode 短信模板CODE。
     * @param aliSmsReq    请求信息。
     * @return 结果
     */
    fun sendSms(templateCode: String, aliSmsReq: AliSmsReq): AliSmsResponse {
        return sendSms(templateCode, listOf(aliSmsReq))
    }

    /**
     * 批量发送短信。
     *
     *
     * 在一次请求中，最多可以向100个手机号码分别发送短信。
     *
     *
     * SendBatchSms接口群发整体请求量限定在128k以内。
     *
     *
     * 文档：https://help.aliyun.com/document_detail/102364.html
     *
     * @param templateCode 短信模板CODE。
     * @param aliSmsReqs   请求信息。
     * @return 结果
     */
    fun sendSms(templateCode: String, aliSmsReqs: List<AliSmsReq>): AliSmsResponse {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        //公共参数
        val action = "SendBatchSms"
        commonParams(params, action)
        //公共参数结束
        val phoneNumbers: MutableList<String> = ArrayList()
        val signNames: MutableList<String> = ArrayList()
        val templateParam: MutableList<Map<String, String>> = ArrayList()
        for (smsReq in aliSmsReqs) {
            phoneNumbers.add(smsReq.phoneNumber)
            signNames.add(smsReq.signName)
            templateParam.add(smsReq.templateParam)
        }
        params.add("PhoneNumberJson", json(phoneNumbers))
        params.add("SignNameJson", json(signNames))
        params.add("TemplateCode", templateCode)
        params.add("TemplateParamJson", json(templateParam))

//    签名
        sign(params)
        val requestCallback = httpEntityCallback<Any>(
            HttpEntity(params, null),
            AliSmsResponse::class.java
        )
        val entity: ResponseEntity<AliSmsResponse> = try {
            execute(
                aliSmsProperties.url,
                HttpMethod.POST,
                requestCallback,
                responseEntityExtractor(AliSmsResponse::class.java)
            )
        } catch (e: Exception) {
            throw SmsException(e)
        } ?: throw SmsException()
        val body = entity.body
        return if (body?.isOk == true) {
            body
        } else {
            var message = body?.message
            val regex = "触发号码天级流控Permits:(\\d+)"
            if (message?.matches(Regex(regex)) == true) {
                message = "每个手机号一天只能获取" + message.replace(regex.toRegex(), "$1") + "条短信"
            }
            throw SmsSysException(message ?: "请求失败")
        }
    }

    /**
     * 查看短信发送记录和发送状态。
     *
     *
     * 本接口的单用户QPS限制为5000次/秒。超过限制，API调用会被限流，这可能会影响您的业务，请合理调用。
     *
     *
     * 文档：https://help.aliyun.com/document_detail/102352.html
     *
     * @param phoneNumber 接收短信的手机号码。
     * @param sendDate    短信发送日期，支持查询最近30天的记录。格式为yyyyMMdd，例如20181225。
     * @param pageSize    分页查看发送记录，指定每页显示的短信记录数量。取值范围为1~50。
     * @param currentPage 分页查看发送记录，指定发送记录的当前页码。
     * @param bizId       发送回执ID，即发送流水号
     * @return 结果
     */
    @JvmOverloads
    fun querySendReport(
        phoneNumber: String, bizId: String = "", sendDate: String = LocalDate.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd")
        ),
        currentPage: Long = 1L, pageSize: Long = 10L
    ): AliSendReportResponse {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        //公共参数
        val action = "QuerySendDetails"
        commonParams(params, action)
        //公共参数结束
        params.add("PhoneNumber", phoneNumber)
        params.add("BizId", bizId)
        params.add("SendDate", sendDate)
        params.add("PageSize", pageSize.toString())
        params.add("CurrentPage", currentPage.toString())

        //签名
        sign(params)
        val requestCallback = httpEntityCallback<Any>(
            HttpEntity(params, null),
            AliSendReportResponse::class.java
        )
        val entity: ResponseEntity<AliSendReportResponse> = try {
            execute(
                aliSmsProperties.url,
                HttpMethod.POST,
                requestCallback,
                responseEntityExtractor(
                    AliSendReportResponse::class.java
                )
            )
        } catch (e: Exception) {
            throw SmsException(e)
        } ?: throw SmsException()
        val body = entity.body
        return if (body?.isOk == true) {
            body
        } else {
            val message = body?.message
            throw SmsSysException(message ?: "请求失败")
        }
    }

    private fun commonParams(
        params: MultiValueMap<String, String>,
        action: String
    ) {
        params.add("AccessKeyId", aliSmsProperties.accessKeyId)
        params.add("Action", action)
        params.add("Format", "json")
        params.add("RegionId", aliSmsProperties.regionId)
        params.add("SignatureMethod", "HMAC-SHA1")
        params.add("SignatureNonce", UUID.randomUUID().toString())
        params.add("SignatureVersion", "1.0")
        params.add(
            "Timestamp",
            LocalDateTime.now(ZoneId.from(ZoneOffset.UTC))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        )
        params.add("Version", "2017-05-25")
    }

    private fun sign(params: MultiValueMap<String, String>) {
        val sortParams = TreeMap(params)
        val it: Iterator<String> = sortParams.keys.iterator()
        val sortQueryStringTmp = StringBuilder()
        while (it.hasNext()) {
            val key = it.next()
            sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=")
                .append(specialUrlEncode(params.getFirst(key) ?: ""))
        }
        val sortedQueryString = sortQueryStringTmp.substring(1) // 去除第一个多余的&符号
        val stringToSign =
            HttpMethod.POST.name + "&" + specialUrlEncode("/") + "&" + specialUrlEncode(
                sortedQueryString
            )
        val sign = sign(aliSmsProperties.accessKeySecret + "&", stringToSign)
        params.add("Signature", sign)
    }

    private fun specialUrlEncode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A")
                .replace("%7E", "~")
        } catch (e: UnsupportedEncodingException) {
            throw SmsException(e)
        }
    }

    private fun sign(accessSecret: String, stringToSign: String): String {
        return try {
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(
                SecretKeySpec(
                    accessSecret.toByteArray(StandardCharsets.UTF_8),
                    "HmacSHA1"
                )
            )
            val signData = mac.doFinal(stringToSign.toByteArray(StandardCharsets.UTF_8))
            String(Base64Utils.encode(signData))
        } catch (e: NoSuchAlgorithmException) {
            throw SmsException(e)
        } catch (e: InvalidKeyException) {
            throw SmsException(e)
        }
    }

}