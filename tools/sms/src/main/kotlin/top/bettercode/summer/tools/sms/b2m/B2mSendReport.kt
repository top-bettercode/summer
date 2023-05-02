package top.bettercode.summer.tools.sms.b2m

/**
 *
 *
 * { "mobile":"15538850001", "smsId":"20170392833833891101", "customSmsId":"1553885000011112",
 * "state":"DELIVRD", "desc":"成功", "receiveTime":"2017-03-15 12:00:00", "submitTime":"2017-03-15
 * 12:00:00", "extendedCode":"123" }
 *
 *
 * @author Peter Wu
 */
class B2mSendReport {
    /**
     * 手机号(必填)
     */
    var mobile: String? = null

    /**
     * 消息ID(选填)
     */
    var smsId: String? = null

    /**
     * 自定义消息ID(选填)
     */
    var customSmsId: String? = null

    /**
     * 状态(必填) ，详见本文档《4.状态报告状态码表》
     */
    var state: String? = null

    /**
     * 状态描述(选填）
     */
    var desc: String? = null

    /**
     * 状态报告返回时间(必填) 格式：yyyy-MM-dd HH:mm:ss
     */
    var receiveTime: String? = null

    /**
     * 信息提交时间(必填) 格式：yyyy-MM-dd HH:mm:ss
     */
    var submitTime: String? = null

    /**
     * 扩展码(选填）
     */
    var extendedCode: String? = null
}
