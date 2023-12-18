package top.bettercode.summer.tools.sms.b2m

/**
 *
 *
 * { "mobile":"15538850000", "smsId":"20170392833833891100", "customSmsId":"20170392833833891100" }
 *
 *
 * @author Peter Wu
 */
class B2mRespData {
    var mobile: String? = null

    /**
     * 平台消息ID
     */
    var smsId: String? = null

    /**
     * 自定义消息ID
     */
    var customSmsId: String? = null
}
