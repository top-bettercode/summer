package top.bettercode.summer.tools.sms.b2m

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

/**
 * 亿美软通短信平台 配置信息
 *
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.sms.b2m")
open class B2mSmsProperties : ClientProperties("亿美软通短信平台") {
    var url = "http://www.btom.cn:8080"
    var appId: String = ""
    var secretKey: String = ""

    /**
     * 模拟发送
     */
    var isMock = false

    /**
     * 系统启动检查余额
     */
    var checkBalance = true

    /**
     * 请求有效时间(必填)[单位:秒]
     */
    var requestValidPeriod = 60 * 10
}
