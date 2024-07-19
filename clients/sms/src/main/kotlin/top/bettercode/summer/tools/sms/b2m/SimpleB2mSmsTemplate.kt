package top.bettercode.summer.tools.sms.b2m

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.util.DigestUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.ApiTemplate
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 亿美软通短信平台 接口请求
 */
@LogMarker(B2mSmsTemplate.MARKER)
open class SimpleB2mSmsTemplate(
    properties: B2mSmsProperties
) : ApiTemplate<B2mSmsProperties>(
    marker = B2mSmsTemplate.MARKER,
    properties = properties
) {

    init {
        val messageConverter: MappingJackson2HttpMessageConverter =
            object : MappingJackson2HttpMessageConverter() {
                override fun canRead(mediaType: MediaType?): Boolean {
                    return true
                }

                override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
                    return true
                }
            }
        val objectMapper = messageConverter.objectMapper
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        val messageConverters: MutableList<HttpMessageConverter<*>> = ArrayList()
        messageConverters.add(ByteArrayHttpMessageConverter())
        messageConverters.add(StringHttpMessageConverter())
        messageConverters.add(AllEncompassingFormHttpMessageConverter())
        messageConverters.add(messageConverter)
        this.messageConverters = messageConverters
    }

    /**
     * 普通个性短信接口
     *
     *
     * 文档：http://www.b2m.cn/static/doc/sms/personalizedsms_or.html
     *
     * 示例：http://ip:port/simpleinter/sendPersonalitySMS?appId=EUCP-EMY-DDDD-3EEEE&timestamp=20170101120000&sign=PIEUDJI987EUID62PKEDSESQEDSEDFSE&extendedCode=123&timerTime=20171211022000&customSmsId=10001&18001098901=天气不错1啊&18001098902=天气不错2啊
     *
     *
     * @param cell    手机号
     * @param content 内容
     * @return 结果
     */
    open fun simpleSendSms(
        cell: String,
        content: String,
        mock: Boolean = properties.isMock
    ): B2mResponse<B2mRespData> {
        return simpleSendSms(Collections.singletonMap(cell, content), mock)
    }

    private val b2mRespDataType =
        object : ParameterizedTypeReference<B2mResponse<B2mRespData>>() {}

    /**
     * 个性短信接口
     *
     *
     * 文档：http://www.b2m.cn/static/doc/sms/personalizedsms_or.html
     *
     * 示例：http://ip:port/simpleinter/sendPersonalitySMS?appId=EUCP-EMY-DDDD-3EEEE&timestamp=20170101120000&sign=PIEUDJI987EUID62PKEDSESQEDSEDFSE&extendedCode=123&timerTime=20171211022000&customSmsId=10001&18001098901=天气不错1啊&18001098902=天气不错2啊
     *
     *
     * @param content 手机号=内容(必填)【可多个】 以手机号为参数名，内容为参数值传输 如：18001000000=端午节快乐,(最多500个)
     * @return 结果
     */
    @JvmOverloads
    open fun simpleSendSms(
        content: Map<String, String>,
        mock: Boolean = properties.isMock
    ): B2mResponse<B2mRespData> {
        if (mock)
            return B2mResponse()
        //    格式：yyyyMMddHHmmss 14位
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params["appId"] = properties.appId
        params["timestamp"] = timestamp
        //    格式：md5(appId+ secretKey + timestamp) 32位
        params["sign"] = DigestUtils.md5DigestAsHex(
            (properties.appId + properties.secretKey + timestamp).toByteArray(
                StandardCharsets.UTF_8
            )
        )
        content.forEach { (key: String, value: String) ->
            if (key.isNotBlank() && value.isNotBlank()) {
                params[key] = value
            }
        }
        //    params.put("timerTime", "");
//    params.put("customSmsId", "");
//    params.put("extendedCode", "");
        val entity: ResponseEntity<B2mResponse<B2mRespData>> =
            exchange(
                properties.url + "/simpleinter/sendPersonalitySMS", HttpMethod.POST,
                HttpEntity(params, null),
                b2mRespDataType
            ) ?: throw clientException()
        return entity.body ?: throw clientException()
    }

    private val b2mSendReportType = object : ParameterizedTypeReference<B2mResponse<B2mSendReport>>() {}

    /**
     *
     * 普通接口 获取状态报告接口
     *
     *
     * 文档：http://www.b2m.cn/static/doc/sms/getpresentation_or.html
     *
     * @param number 获取数量 最多500个，默认500个
     * @return 结果
     */
    @JvmOverloads
    open fun simpleQuerySendReport(number: Int = 500): B2mResponse<B2mSendReport> {
        //    格式：yyyyMMddHHmmss 14位
        val timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val timestamp = LocalDateTime.now().format(timeFormatter)
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params["appId"] = properties.appId
        params["timestamp"] = timestamp
        //    格式：md5(appId+ secretKey + timestamp) 32位
        params["sign"] = DigestUtils.md5DigestAsHex(
            (properties.appId + properties.secretKey + timestamp).toByteArray(
                StandardCharsets.UTF_8
            )
        )
        params["number"] = number.toString()

        val entity: ResponseEntity<B2mResponse<B2mSendReport>> =
            exchange(
                properties.url + "/simpleinter/getReport", HttpMethod.POST,
                HttpEntity(params, null),
                b2mSendReportType
            ) ?: throw clientException()
        return entity.body ?: throw clientException()

    }

    /**
     *
     * 状态报告重新获取接口
     *
     *
     * 文档：http://www.b2m.cn/static/doc/sms/statusReport.html
     *
     * @param startTime 状态报告开始时间(必填，对应短信提交时间，只能为当前时间30天前范围，不能大于当前时间) 格式：yyyyMMddHHmmss 14位
     * @param endTime 状态报告结束时间(必填,对应短信提交时间,只能为当前时间30天前范围,不能大于当前时间,应大于开始时间,开始结束时间最大间隔为10分钟,建议5分钟以内) 格式：yyyyMMddHHmmss 14位
     * @param smsId 短信的smsId(选填)，多个用半角逗号分隔，最多1000个，如 123123123,321321321
     * @return 结果
     */
    @JvmOverloads
    open fun retrieveReport(
        startTime: LocalDateTime,
        endTime: LocalDateTime = startTime.plusMinutes(10),
        smsId: String = ""
    ): Boolean {
        //    格式：yyyyMMddHHmmss 14位
        val timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val timestamp = LocalDateTime.now().format(timeFormatter)
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params["appId"] = properties.appId
        params["timestamp"] = timestamp
        //    格式：md5(appId+ secretKey + timestamp) 32位
        params["sign"] = DigestUtils.md5DigestAsHex(
            (properties.appId + properties.secretKey + timestamp).toByteArray(
                StandardCharsets.UTF_8
            )
        )
        params["startTime"] = startTime.format(timeFormatter)
        params["endTime"] = endTime.format(timeFormatter)
        params["smsId"] = smsId

        val entity: ResponseEntity<String> = try {
            exchange(
                properties.url + "/report/retrieveReport", HttpMethod.POST,
                HttpEntity(params, null),
                String::class.java
            )
        } catch (e: Exception) {
            throw clientException(e)
        } ?: throw clientException()
        return if (B2mResponse.SUCCESS == entity.body) {
            true
        } else {
            val message = B2mResponse.getMessage(entity.body)
            throw clientSysException(message)
        }

    }
}