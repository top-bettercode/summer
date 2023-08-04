package top.bettercode.summer.tools.sms.b2m

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.type.TypeFactory
import org.springframework.http.*
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import top.bettercode.summer.tools.lang.util.AESUtil.decrypt
import top.bettercode.summer.tools.lang.util.AESUtil.encrypt
import top.bettercode.summer.tools.lang.util.StringUtil.gzip
import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.tools.lang.util.StringUtil.readJson
import top.bettercode.summer.tools.lang.util.StringUtil.ungzip
import top.bettercode.summer.tools.sms.SmsException
import top.bettercode.summer.tools.sms.SmsSysException
import top.bettercode.summer.tools.sms.SmsTemplate
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 亿美软通短信平台 接口请求
 */
class B2mSmsTemplate(
        private val b2mProperties: B2mSmsProperties
) : SmsTemplate(
        "第三方平台",
        "亿美软通短信平台",
        LOG_MARKER_STR,
        b2mProperties.connectTimeout,
        b2mProperties.readTimeout,
        { bytes -> ungzip(decrypt(bytes, b2mProperties.secretKey)) },
        { bytes -> ungzip(decrypt(bytes, b2mProperties.secretKey)) }
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
        setMessageConverters(messageConverters)
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
    fun sendSms(
            cell: String,
            content: String,
            mock: Boolean = b2mProperties.isMock
    ): B2mResponse<B2mRespData> {
        return sendSms(Collections.singletonMap(cell, content), mock)
    }

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
    fun sendSms(
            content: Map<String, String>,
            mock: Boolean = b2mProperties.isMock
    ): B2mResponse<B2mRespData> {
        if (mock)
            return B2mResponse()
        val headers = HttpHeaders()
        headers.add("appId", b2mProperties.appId)
        headers.add("gzip", "on")
        val params: MutableMap<String, Any> = mutableMapOf()
        val smses: MutableList<Map<String, Any>> = mutableListOf()
        content.forEach { (key: String, value: String) ->
            smses.add(
                    mapOf<String, Any>(
                            //          "customSmsId" to "",
                            //          "timerTime" to "",
                            //          "extendedCode" to "",
                            "mobile" to key,
                            "content" to value
                    )
            )
        }
        params["smses"] = smses
        params["requestTime"] = System.currentTimeMillis()
        params["requestValidPeriod"] = b2mProperties.requestValidPeriod
        val json = json(params)
        var data = json.toByteArray(StandardCharsets.UTF_8)
        data = gzip(data)
        data = encrypt(data, b2mProperties.secretKey)
        val requestCallback = httpEntityCallback<Any>(
                HttpEntity(data, headers),
                ByteArray::class.java
        )
        val entity: ResponseEntity<ByteArray> = try {
            execute(
                    b2mProperties.url + "/inter/sendPersonalityAllSMS", HttpMethod.POST,
                    requestCallback,
                    responseEntityExtractor<ByteArray>(ByteArray::class.java)
            )
        } catch (e: Exception) {
            throw SmsException(e)
        } ?: throw SmsException()

        return if (entity.statusCode.is2xxSuccessful) {
            val code = entity.headers.getFirst("result")
            if (B2mResponse.SUCCESS == code) {
                var respData = entity.body
                respData = decrypt(respData!!, b2mProperties.secretKey)
                respData = ungzip(respData)
                val datas: List<B2mRespData> = readJson<MutableList<B2mRespData>>(
                        respData,
                        TypeFactory.defaultInstance().constructCollectionType(
                                MutableList::class.java, B2mRespData::class.java
                        )
                )
                B2mResponse(datas)
            } else {
                val message = B2mResponse.getMessage(code)
                throw SmsSysException(message ?: "请求失败")
            }
        } else {
            throw SmsException()
        }
    }

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
    fun querySendReport(number: Int = 500): List<B2mSendReport> {
        val headers = HttpHeaders()
        headers.add("appId", b2mProperties.appId)
        headers.add("gzip", "on")
        val params: MutableMap<String, Any> = mutableMapOf()
        params["number"] = number
        params["requestTime"] = System.currentTimeMillis()
        params["requestValidPeriod"] = b2mProperties.requestValidPeriod
        val json = json(params)
        var data = json.toByteArray(StandardCharsets.UTF_8)
        data = gzip(data)
        data = encrypt(data, b2mProperties.secretKey)
        val requestCallback = httpEntityCallback<Any>(
                HttpEntity(data, headers),
                ByteArray::class.java
        )
        val entity: ResponseEntity<ByteArray> = try {
            execute(
                    b2mProperties.url + "/inter/getReport", HttpMethod.POST,
                    requestCallback,
                    responseEntityExtractor<ByteArray>(ByteArray::class.java)
            )
        } catch (e: Exception) {
            throw SmsException(e)
        } ?: throw SmsException()

        return if (entity.statusCode.is2xxSuccessful) {
            val code = entity.headers.getFirst("result")
            if (B2mResponse.SUCCESS == code) {
                var respData = entity.body
                respData = decrypt(respData!!, b2mProperties.secretKey)
                respData = ungzip(respData)
                readJson<List<B2mSendReport>>(
                        respData,
                        TypeFactory.defaultInstance().constructCollectionType(
                                MutableList::class.java, B2mSendReport::class.java
                        )
                )
            } else {
                val message = B2mResponse.getMessage(code)
                throw SmsSysException(message ?: "请求失败")
            }
        } else {
            throw SmsException()
        }
    }

}