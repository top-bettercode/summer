package top.bettercode.summer.tools.sms

/**
 * @author Peter Wu
 */
interface SmsResponse {
    /**
     * @return 是否成功
     */
    val isOk: Boolean

    /**
     * @return 响应消息
     */
    val message: String?
}
