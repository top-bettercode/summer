package top.bettercode.summer.tools.sms.aliyun

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import top.bettercode.summer.test.BaseTest

/**
 * @author Peter Wu
 */
@Disabled
internal class AliSmsTemplateTest : BaseTest() {
    @Autowired
    lateinit var aliSmsTemplate: AliSmsTemplate

    @Value("\${mobile}")
    lateinit var mobile: String

    @Disabled
    @Test
    fun sendSms() {
        aliSmsTemplate.sendSms("SMS_126359654", AliSmsReq(mobile, "云图提货", mapOf(
                Pair("code", "123456"))))
    }

    @Test
    fun querySendReport() {
        aliSmsTemplate.querySendReport(mobile)
    }

    @Test
    fun querySmsSignList() {
        aliSmsTemplate.querySmsSignList()
    }

    @Test
    fun querySmsTemplateList() {
        aliSmsTemplate.querySmsTemplateList()
    }
}