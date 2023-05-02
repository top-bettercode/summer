package top.bettercode.summer.tools.sms.aliyun

import com.fasterxml.jackson.annotation.JsonProperty

class SmsSignListItem {
    /**
     * 签名名称。
     */
    @JsonProperty("SignName")
    var signName: String? = null

    /**
     * 签名审批状态。取值：
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
     * 短信签名的创建日期和时间，格式为yyyy-MM-dd HH:mm:ss。
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
     * 签名场景类型。
     */
    @JsonProperty("BusinessType")
    var businessType: String? = null

    /**
     * 工单ID。
     */
    @JsonProperty("OrderId")
    var orderId: String? = null
    override fun toString(): String {
        return "SmsSignListItem{" +
                "signName = '" + signName + '\'' +
                ",auditStatus = '" + auditStatus + '\'' +
                ",createDate = '" + createDate + '\'' +
                ",reason = '" + reason + '\'' +
                ",businessType = '" + businessType + '\'' +
                ",orderId = '" + orderId + '\'' +
                "}"
    }
}