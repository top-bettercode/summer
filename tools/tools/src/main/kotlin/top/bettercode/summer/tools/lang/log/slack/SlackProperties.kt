package top.bettercode.summer.tools.lang.log.slack

import top.bettercode.summer.tools.lang.log.AlarmProperties

/**
 * slack 配置
 *
 * @author Peter Wu
 */
open class SlackProperties : AlarmProperties() {
    var authToken: String = ""
    var channel: String = ""
    var timeoutChannel = "timeout"
}