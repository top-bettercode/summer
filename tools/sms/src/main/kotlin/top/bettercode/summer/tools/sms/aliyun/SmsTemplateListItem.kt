package top.bettercode.summer.tools.sms.aliyun

import com.fasterxml.jackson.annotation.JsonProperty

class SmsTemplateListItem {
    /**
     * 短信模板CODE。
     *
     *
     * 您可以登录短信服务控制台，选择国内消息或国际/港澳台消息，在模板管理页签中查看模板CODE。也可以通过AddSmsTemplate接口获取模板CODE。
     */
    @JsonProperty("TemplateCode")
    var templateCode: String? = null

    /**
     * 模板名称。
     */
    @JsonProperty("TemplateName")
    var templateName: String? = null

    /**
     * 模板类型。取值：
     *
     *
     * 0：短信通知。
     *
     *
     * 1：推广短信。
     *
     *
     * 2：验证码短信。
     *
     *
     * 6：国际/港澳台短信。
     *
     *
     * 7：数字短信。
     */
    @JsonProperty("TemplateType")
    var templateType = 0

    /**
     * 模板审批状态。取值：
     *
     *
     * AUDIT_STATE_INIT：审核中。
     *
     *
     * AUDIT_STATE_PASS：审核通过。
     *
     *
     * AUDIT_STATE_NOT_PASS：审核未通过，请在返回参数Reason中查看审核未通过原因。
     *
     *
     * AUDIT_STATE_CANCEL：取消审核。
     */
    @JsonProperty("AuditStatus")
    var auditStatus: String? = null

    /**
     * 模板内容。
     */
    @JsonProperty("TemplateContent")
    var templateContent: String? = null

    /**
     * 短信模板的创建时间，格式为yyyy-MM-dd HH:mm:ss。
     */
    @JsonProperty("CreateDate")
    var createDate: String? = null

    /**
     * 审核备注。
     *
     *
     * 如果审核状态为审核通过或审核中，参数Reason显示为“无审核备注”。
     *
     *
     * 如果审核状态为审核未通过，参数Reason显示审核的具体原因。
     */
    @JsonProperty("Reason")
    var reason: Reason? = null

    /**
     * 工单ID。
     */
    @JsonProperty("OrderId")
    var orderId: String? = null
    override fun toString(): String {
        return "SmsTemplateListItem{" +
                "templateCode = '" + templateCode + '\'' +
                ",templateName = '" + templateName + '\'' +
                ",templateType = '" + templateType + '\'' +
                ",auditStatus = '" + auditStatus + '\'' +
                ",templateContent = '" + templateContent + '\'' +
                ",createDate = '" + createDate + '\'' +
                ",reason = '" + reason + '\'' +
                ",orderId = '" + orderId + '\'' +
                "}"
    }
}