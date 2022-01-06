package top.bettercode.sms.b2m

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.common.collect.ImmutableMap
import io.micrometer.core.instrument.util.JsonUtils
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.util.DigestUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import top.bettercode.lang.util.AESUtil.decrypt
import top.bettercode.lang.util.AESUtil.encrypt
import top.bettercode.lang.util.StringUtil.gzip
import top.bettercode.lang.util.StringUtil.json
import top.bettercode.lang.util.StringUtil.readJson
import top.bettercode.lang.util.StringUtil.ungzip
import top.bettercode.sms.SmsException
import top.bettercode.sms.SmsSysException
import top.bettercode.sms.SmsTemplate
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 亿美软通短信平台 接口请求
 */
class B2mSmsTemplate(
    private val b2mProperties: B2mSmsProperties
) : SmsTemplate(
    "第三方接口", "亿美软通短信平台", LOG_MARKER_STR, b2mProperties.connectTimeout,
    b2mProperties.readTimeout
) {
    private val log = LoggerFactory.getLogger(B2mSmsTemplate::class.java)

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
        messageConverters.add(ByteArrayHttpMessageConverter())
        messageConverters.add(StringHttpMessageConverter())
        messageConverters.add(AllEncompassingFormHttpMessageConverter())
        messageConverters.add(messageConverter)
        setMessageConverters(messageConverters)
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
    fun simpleSendSms(cell: String, content: String): B2mResponse<B2mRespData> {
        return simpleSendSms(Collections.singletonMap(cell, content))
    }

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
    fun simpleSendSms(content: Map<String, String>): B2mResponse<B2mRespData> {
        //    格式：yyyyMMddHHmmss 14位
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params["appId"] = b2mProperties.appId
        params["timestamp"] = timestamp
        //    格式：md5(appId+ secretKey + timestamp) 32位
        params["sign"] = DigestUtils.md5DigestAsHex(
            (b2mProperties.appId + b2mProperties.secretKey + timestamp).toByteArray(
                StandardCharsets.UTF_8
            )
        )
        content.forEach { (key: String, value: String) ->
            if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                params[key] = value
            }
        }
        //    params.put("timerTime", "");
//    params.put("customSmsId", "");
//    params.put("extendedCode", "");
        val javaType = TypeFactory.defaultInstance().constructParametricType(
            B2mResponse::class.java, B2mRespData::class.java
        )
        val requestCallback = httpEntityCallback<Any>(
            HttpEntity(params, null),
            javaType
        )
        val responseEntityExtractor = responseEntityExtractor<B2mResponse<B2mRespData>>(javaType)
        val entity: ResponseEntity<B2mResponse<B2mRespData>> = try {
            execute(
                b2mProperties.url + "/simpleinter/sendPersonalitySMS", HttpMethod.POST,
                requestCallback,
                responseEntityExtractor
            )
        } catch (e: Exception) {
            throw SmsException(e)
        } ?: throw SmsException()

        return if (entity.statusCode.is2xxSuccessful) {
            val body = entity.body
            if (body?.isOk == true) {
                body
            } else {
                val message = body?.message
                throw SmsSysException(message ?: "请求失败")
            }
        } else {
            throw SmsException()
        }
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
    fun sendSms(cell: String, content: String): B2mResponse<B2mRespData> {
        return sendSms(Collections.singletonMap(cell, content))
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
    fun sendSms(content: Map<String, String>): B2mResponse<B2mRespData> {
        val headers = HttpHeaders()
        headers.add("appId", b2mProperties.appId)
        headers.add("gzip", "on")
        val params: MutableMap<String, Any> = mutableMapOf()
        val smses: MutableList<Map<String, Any>> = mutableListOf()
        content.forEach { (key: String, value: String) ->
            smses.add(
                ImmutableMap.of<String, Any>(
                    //          "customSmsId", "",
                    //          "timerTime", "",
                    //          "extendedCode", "",
                    "mobile", key,
                    "content", value
                )
            )
        }
        params["smses"] = smses
        params["requestTime"] = System.currentTimeMillis()
        params["requestValidPeriod"] = b2mProperties.requestValidPeriod
        val json = json(params)
        log.info(LOG_MARKER, "params:{}", json)
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
                log.info(
                    LOG_MARKER,
                    "result:{}",
                    JsonUtils.prettyPrint(
                        String(respData).split("\n").joinToString("") { it.trim() })
                )
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
     * 普通接口 获取状态报告接口
     *
     *
     * 文档：http://www.b2m.cn/static/doc/sms/getpresentation_or.html
     *
     * @param number 获取数量 最多500个，默认500个
     * @return 结果
     */
    @JvmOverloads
    fun simpleQuerySendReport(number: Int = 500): B2mResponse<B2mSendReport> {
        //    格式：yyyyMMddHHmmss 14位
        val timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val timestamp = LocalDateTime.now().format(timeFormatter)
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params["appId"] = b2mProperties.appId
        params["timestamp"] = timestamp
        //    格式：md5(appId+ secretKey + timestamp) 32位
        params["sign"] = DigestUtils.md5DigestAsHex(
            (b2mProperties.appId + b2mProperties.secretKey + timestamp).toByteArray(
                StandardCharsets.UTF_8
            )
        )
        params["number"] = number.toString();
        val javaType = TypeFactory.defaultInstance().constructParametricType(
            B2mResponse::class.java, B2mSendReport::class.java
        )
        val requestCallback = httpEntityCallback<Any>(
            HttpEntity(params, null),
            javaType
        )
        val responseEntityExtractor = responseEntityExtractor<B2mResponse<B2mSendReport>>(javaType)
        val entity: ResponseEntity<B2mResponse<B2mSendReport>> = try {
            execute(
                b2mProperties.url + "/simpleinter/getReport", HttpMethod.POST,
                requestCallback,
                responseEntityExtractor
            )
        } catch (e: Exception) {
            throw SmsException(e)
        } ?: throw SmsException()

        return if (entity.statusCode.is2xxSuccessful) {
            val body = entity.body
            if (body?.isOk == true) {
                body
            } else {
                val message = body?.message
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
        log.info(LOG_MARKER, "params:{}", json)
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
                log.info(
                    LOG_MARKER,
                    "result:{}",
                    JsonUtils.prettyPrint(
                        String(respData).split("\n").joinToString("") { it.trim() })
                )
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

    /**
     *
     * 安全接口 获取状态报告接口
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
    fun retrieveReport(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        smsId: String = ""
    ): Boolean {
        //    格式：yyyyMMddHHmmss 14位
        val timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val timestamp = LocalDateTime.now().format(timeFormatter)
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params["appId"] = b2mProperties.appId
        params["timestamp"] = timestamp
        //    格式：md5(appId+ secretKey + timestamp) 32位
        params["sign"] = DigestUtils.md5DigestAsHex(
            (b2mProperties.appId + b2mProperties.secretKey + timestamp).toByteArray(
                StandardCharsets.UTF_8
            )
        )
        params["startTime"] = startTime.format(timeFormatter);
        params["endTime"] = endTime.format(timeFormatter);
        params["smsId"] = smsId;
        val requestCallback = httpEntityCallback<Any>(
            HttpEntity(params, null),
            String::class.java
        )
        val entity: ResponseEntity<String> = try {
            execute(
                b2mProperties.url + "/report/retrieveReport", HttpMethod.POST,
                requestCallback,
                responseEntityExtractor<String>(String::class.java)
            )
        } catch (e: Exception) {
            throw SmsException(e)
        } ?: throw SmsException()
        return if (entity.statusCode.is2xxSuccessful) {
            if (B2mResponse.SUCCESS == entity.body) {
                true
            } else {
                val message = B2mResponse.getMessage(entity.body)
                throw SmsSysException(message ?: "请求失败")
            }
        } else {
            throw SmsException()
        }

    }
}