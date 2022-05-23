package top.bettercode.sms.aliyun;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmsSignListItem {

  /**
   * 签名名称。
   */
  @JsonProperty("SignName")
  private String signName;

  /**
   * 签名审批状态。取值：
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
   * 短信签名的创建日期和时间，格式为yyyy-MM-dd HH:mm:ss。
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
   * 签名场景类型。
   */
  @JsonProperty("BusinessType")
  private String businessType;

  /**
   * 工单ID。
   */
  @JsonProperty("OrderId")
  private String orderId;

  public void setSignName(String signName) {
    this.signName = signName;
  }

  public String getSignName() {
    return signName;
  }

  public void setAuditStatus(String auditStatus) {
    this.auditStatus = auditStatus;
  }

  public String getAuditStatus() {
    return auditStatus;
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

  public void setBusinessType(String businessType) {
    this.businessType = businessType;
  }

  public String getBusinessType() {
    return businessType;
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
        "SmsSignListItem{" +
            "signName = '" + signName + '\'' +
            ",auditStatus = '" + auditStatus + '\'' +
            ",createDate = '" + createDate + '\'' +
            ",reason = '" + reason + '\'' +
            ",businessType = '" + businessType + '\'' +
            ",orderId = '" + orderId + '\'' +
            "}";
  }
}