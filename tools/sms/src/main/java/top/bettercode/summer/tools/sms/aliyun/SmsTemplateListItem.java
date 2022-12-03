package top.bettercode.summer.tools.sms.aliyun;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmsTemplateListItem {

  /**
   * 短信模板CODE。
   * <p>
   * 您可以登录短信服务控制台，选择国内消息或国际/港澳台消息，在模板管理页签中查看模板CODE。也可以通过AddSmsTemplate接口获取模板CODE。
   */
  @JsonProperty("TemplateCode")
  private String templateCode;

  /**
   * 模板名称。
   */
  @JsonProperty("TemplateName")
  private String templateName;

  /**
   * 模板类型。取值：
   * <p>
   * 0：短信通知。
   * <p>
   * 1：推广短信。
   * <p>
   * 2：验证码短信。
   * <p>
   * 6：国际/港澳台短信。
   * <p>
   * 7：数字短信。
   */
  @JsonProperty("TemplateType")
  private int templateType;
  /**
   * 模板审批状态。取值：
   * <p>
   * AUDIT_STATE_INIT：审核中。
   * <p>
   * AUDIT_STATE_PASS：审核通过。
   * <p>
   * AUDIT_STATE_NOT_PASS：审核未通过，请在返回参数Reason中查看审核未通过原因。
   * <p>
   * AUDIT_STATE_CANCEL：取消审核。
   */
  @JsonProperty("AuditStatus")
  private String auditStatus;

  /**
   * 模板内容。
   */
  @JsonProperty("TemplateContent")
  private String templateContent;

  /**
   * 短信模板的创建时间，格式为yyyy-MM-dd HH:mm:ss。
   */
  @JsonProperty("CreateDate")
  private String createDate;
  /**
   * 审核备注。
   * <p>
   * 如果审核状态为审核通过或审核中，参数Reason显示为“无审核备注”。
   * <p>
   * 如果审核状态为审核未通过，参数Reason显示审核的具体原因。
   */
  @JsonProperty("Reason")
  private Reason reason;

  /**
   * 工单ID。
   */
  @JsonProperty("OrderId")
  private String orderId;

  public void setTemplateCode(String templateCode) {
    this.templateCode = templateCode;
  }

  public String getTemplateCode() {
    return templateCode;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateType(int templateType) {
    this.templateType = templateType;
  }

  public int getTemplateType() {
    return templateType;
  }

  public void setAuditStatus(String auditStatus) {
    this.auditStatus = auditStatus;
  }

  public String getAuditStatus() {
    return auditStatus;
  }

  public void setTemplateContent(String templateContent) {
    this.templateContent = templateContent;
  }

  public String getTemplateContent() {
    return templateContent;
  }

  public void setCreateDate(String createDate) {
    this.createDate = createDate;
  }

  public String getCreateDate() {
    return createDate;
  }

  public void setReason(Reason reason) {
    this.reason = reason;
  }

  public Reason getReason() {
    return reason;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getOrderId() {
    return orderId;
  }

  @Override
  public String toString() {
    return
        "SmsTemplateListItem{" +
            "templateCode = '" + templateCode + '\'' +
            ",templateName = '" + templateName + '\'' +
            ",templateType = '" + templateType + '\'' +
            ",auditStatus = '" + auditStatus + '\'' +
            ",templateContent = '" + templateContent + '\'' +
            ",createDate = '" + createDate + '\'' +
            ",reason = '" + reason + '\'' +
            ",orderId = '" + orderId + '\'' +
            "}";
  }
}