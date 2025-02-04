package top.bettercode.summer.tools.sms.b2m

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.type.TypeFactory
import org.springframework.http.*
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.ApiTemplate
import top.bettercode.summer.tools.lang.util.AESUtil.decrypt
import top.bettercode.summer.tools.lang.util.AESUtil.encrypt
import top.bettercode.summer.tools.lang.util.StringUtil.gzip
import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.lang.util.StringUtil.readJson
import top.bettercode.summer.tools.lang.util.StringUtil.ungzip
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 亿美软通短信平台 接口请求
 */
@LogMarker(B2mSmsTemplate.MARKER)
open class B2mSmsTemplate(
    properties: B2mSmsProperties
) : ApiTemplate<B2mSmsProperties>(
    marker = MARKER,
    properties = properties,
    requestDecrypt = { bytes -> ungzip(decrypt(bytes, properties.secretKey)) },
    responseDecrypt = { bytes -> ungzip(decrypt(bytes, properties.secretKey)) }
) {

    companion object {
        const val MARKER = "sms"
    }

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
     * 安全接口 个性短信接口【全属性个性】
     *
     *
     * 文档：http://www.b2m.cn/static/doc/sms/moresms_custom.html
     *
     * @param cell    手机号
     * @param content 内容
     * @return 结果
     */
    @JvmOverloads
    open fun sendSms(
        cell: String,
        content: String,
        mock: Boolean = properties.isMock
    ): B2mResponse<B2mRespData> {
        return sendSms(Collections.singletonMap(cell, content), mock)
    }

    private val b2mRespDataType = TypeFactory.defaultInstance().constructCollectionType(
        MutableList::class.java, B2mRespData::class.java
    )

    /**
     * 安全接口 个性短信接口【全属性个性】
     *
     *
     * 文档：http://www.b2m.cn/static/doc/sms/moresms_custom.html
     *
     * @param content 手机号=内容(必填)【可多个】 以手机号为参数名，内容为参数值传输 如：18001000000=端午节快乐,(最多500个)
     * @return 结果
     */
    @JvmOverloads
    open fun sendSms(
        content: Map<String, String>,
        mock: Boolean = properties.isMock
    ): B2mResponse<B2mRespData> {
        if (mock)
            return B2mResponse()
        val headers = HttpHeaders()
        headers.add("appId", properties.appId)
        headers.add("gzip", "on")
        val params: MutableMap<String, Any> = mutableMapOf()
        val smses: MutableList<Map<String, Any>> = mutableListOf()
        content.forEach { (key: String, value: String) ->
            smses.add(
                mapOf<String, Any>(
//                    "customSmsId" to UUID.randomUUID().toString().replace("-", ""),
                    //          "timerTime" to "",
                    //          "extendedCode" to "",
                    "mobile" to key,
                    "content" to value
                )
            )
        }
        params["smses"] = smses
        params["requestTime"] = System.currentTimeMillis()
        params["requestValidPeriod"] = properties.requestValidPeriod
        val json = json(params)
        var data = json.toByteArray(StandardCharsets.UTF_8)
        data = gzip(data)
        data = encrypt(data, properties.secretKey)

        val entity: ResponseEntity<ByteArray> =
            exchange(
                properties.url + "/inter/sendPersonalityAllSMS", HttpMethod.POST,
                HttpEntity(data, headers),
                ByteArray::class.java
            ) ?: throw clientException()

        val code = entity.headers.getFirst("result")
        return if (B2mResponse.SUCCESS == code) {
            var respData = entity.body
            respData = decrypt(respData!!, properties.secretKey)
            respData = ungzip(respData)
            val datas: List<B2mRespData> = readJson<MutableList<B2mRespData>>(
                respData,
                b2mRespDataType
            )
            B2mResponse(datas)
        } else {
            val message = B2mResponse.getMessage(code)
            throw clientSysException(message)
        }
    }

    private val b2mSendReportType = TypeFactory.defaultInstance().constructCollectionType(
        MutableList::class.java, B2mSendReport::class.java
    )

    /**
     *
     * 安全接口 获取状态报告接口
     *
     *
     * 文档：http://www.b2m.cn/static/doc/sms/getpresentation.html
     *
     * @param number 获取状态报告数量(非必填) 默认每次请求给500个
     * @return 结果
     */
    @JvmOverloads
    open fun querySendReport(number: Int = 500): List<B2mSendReport> {
        val headers = HttpHeaders()
        headers.add("appId", properties.appId)
        headers.add("gzip", "on")
        val params: MutableMap<String, Any> = mutableMapOf()
        params["number"] = number
        params["requestTime"] = System.currentTimeMillis()
        params["requestValidPeriod"] = properties.requestValidPeriod
        val json = json(params)
        var data = json.toByteArray(StandardCharsets.UTF_8)
        data = gzip(data)
        data = encrypt(data, properties.secretKey)
        val entity: ResponseEntity<ByteArray> =
            exchange(
                properties.url + "/inter/getReport", HttpMethod.POST,
                HttpEntity(data, headers),
                ByteArray::class.java
            ) ?: throw clientException()

        val code = entity.headers.getFirst("result")
        return if (B2mResponse.SUCCESS == code) {
            var respData = entity.body
            respData = decrypt(respData!!, properties.secretKey)
            respData = ungzip(respData)
            readJson(
                respData,
                b2mSendReportType
            )
        } else {
            val message = B2mResponse.getMessage(code)
            throw clientSysException(message)
        }
    }

    /**
     *
     * 获取余额接口
     *
     * 文档：http://www.b2m.cn/static/doc/sms/getbalance.html
     *
     * @return 结果
     */
    open fun getBalance(): B2mBalance {
        val headers = HttpHeaders()
        headers.add("appId", properties.appId)
        headers.add("gzip", "on")
        headers.add("encode", "UTF-8")
        val params: MutableMap<String, Any> = mutableMapOf()
        params["requestTime"] = System.currentTimeMillis()
        params["requestValidPeriod"] = properties.requestValidPeriod
        val json = json(params)
        var data = json.toByteArray(StandardCharsets.UTF_8)
        data = gzip(data)
        data = encrypt(data, properties.secretKey)
        val entity: ResponseEntity<ByteArray> =
            exchange(
                properties.url + "/inter/getBalance", HttpMethod.POST,
                HttpEntity(data, headers),
                ByteArray::class.java
            ) ?: throw clientException()

        val code = entity.headers.getFirst("result")
        return if (B2mResponse.SUCCESS == code) {
            var respData = entity.body
            respData = decrypt(respData!!, properties.secretKey)
            respData = ungzip(respData)
            readJson(
                respData,
                B2mBalance::class.java
            )
        } else {
            val message = B2mResponse.getMessage(code)
            throw clientSysException(message)
        }
    }

    fun checkBalance(): Boolean {
        val balance = getBalance().balance
        val hasBalance = balance != null && balance > 1000
        if (!hasBalance)
            log.error("亿美短信余额不足，请及时充值，当前余额：$balance")
        else
            log.info("亿美短信余额：$balance")
        return hasBalance
    }

}