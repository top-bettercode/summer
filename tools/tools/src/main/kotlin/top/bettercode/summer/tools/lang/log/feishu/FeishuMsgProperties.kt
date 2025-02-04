package top.bettercode.summer.tools.lang.log.feishu

import top.bettercode.summer.tools.lang.log.AlarmProperties

/**
 * 飞书 配置
 *
 * @author Peter Wu
 */
open class FeishuMsgProperties : AlarmProperties() {
    var appId: String = ""
    var appSecret: String = ""

    var chat: String = ""
    var timeoutChat: String = "timeout"

    open var chatHook: FeishuWebHook? = null
    open var timeoutChatHook: FeishuWebHook? = null
}