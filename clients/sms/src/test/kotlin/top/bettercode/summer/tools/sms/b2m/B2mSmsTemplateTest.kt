package top.bettercode.summer.tools.sms.b2m

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import top.bettercode.summer.test.BaseTest
import java.time.LocalDateTime
import java.util.*

/**
 * @author Peter Wu
 */
//@Disabled
internal class B2mSmsTemplateTest : BaseTest() {
    @Autowired
    lateinit var b2mTemplate: B2mSmsTemplate

    @Autowired
    lateinit var simpleB2mTemplate: SimpleB2mSmsTemplate

    @Value("\${mobile:}")
    var mobile: String? = null

    @Test
    fun simpleSendSms() {
        simpleB2mTemplate.simpleSendSms(Collections.singletonMap(mobile,
                "【云图复肥】客户您好，2020年09月22日您的订单在应城市新都化工有限责任公司复合肥工厂12已发货物料：45%241407新氨化掺混腐植酸中氯黑， 数量：12.700，单位：吨，请注意查收，如有异议请在48小时内联系热线028-83950223。"))
    }

    @Test
    fun sendSms() {
        b2mTemplate.sendSms(Collections.singletonMap(mobile,
                "【云图复肥】客户您好，2020年09月22日您的订单在应城市新都化工有限责任公司复合肥工厂12已发货物料：45%241407新氨化掺混腐植酸中氯黑， 数量：12.700，单位：吨，请注意查收，如有异议请在48小时内联系热线028-83950223。"))
    }

    @Test
    fun simpleQuerySendReport() {
        simpleB2mTemplate.simpleQuerySendReport()
    }

    @Test
    fun querySendReport() {
        b2mTemplate.querySendReport()
    }

    @Test
    fun retrieveReport() {
        simpleB2mTemplate.retrieveReport(LocalDateTime.of(2022, 3, 30, 9, 0))
    }

    @Test
    fun getBalance() {
        b2mTemplate.getBalance()
    }

}