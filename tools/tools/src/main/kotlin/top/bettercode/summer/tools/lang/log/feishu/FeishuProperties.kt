package top.bettercode.summer.tools.lang.log.feishu

import top.bettercode.summer.tools.lang.log.AlarmProperties

/**
 * slack 配置
 *
 * @author Peter Wu
 */
open class FeishuProperties : AlarmProperties() {
    var appId: String = ""
    var appSecret: String = ""

    var chat: String = ""
    var timeoutChat: String = "timeout"

    var chatHook: FeishuWebHook? = null
    var timeoutChatHook: FeishuWebHook? = null
}