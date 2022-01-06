package top.bettercode.sms.b2m;

/**
 * <p>
 * { "mobile":"15538850001", "smsId":"20170392833833891101", "customSmsId":"1553885000011112",
 * "state":"DELIVRD", "desc":"成功", "receiveTime":"2017-03-15 12:00:00", "submitTime":"2017-03-15
 * 12:00:00", "extendedCode":"123" }
 * </p>
 *
 * @author Peter Wu
 */
public class B2mSendReport {

  /**
   * 手机号(必填)
   */
  private String mobile;
  /**
   * 消息ID(选填)
   */
  private String smsId;
  /**
   * 自定义消息ID(选填)
   */
  private String customSmsId;
  /**
   * 状态(必填) ，详见本文档《4.状态报告状态码表》
   */
  private String state;
  /**
   * 状态描述(选填）
   */
  private String desc;
  /**
   * 状态报告返回时间(必填) 格式：yyyy-MM-dd HH:mm:ss
   */
  private String receiveTime;
  /**
   * 信息提交时间(必填) 格式：yyyy-MM-dd HH:mm:ss
   */
  private String submitTime;
  /**
   * 扩展码(选填）
   */
  private String extendedCode;

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getSmsId() {
    return smsId;
  }

  public void setSmsId(String smsId) {
    this.smsId = smsId;
  }

  public String getCustomSmsId() {
    return customSmsId;
  }

  public void setCustomSmsId(String customSmsId) {
    this.customSmsId = customSmsId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public String getReceiveTime() {
    return receiveTime;
  }

  public void setReceiveTime(String receiveTime) {
    this.receiveTime = receiveTime;
  }

  public String getSubmitTime() {
    return submitTime;
  }

  public void setSubmitTime(String submitTime) {
    this.submitTime = submitTime;
  }

  public String getExtendedCode() {
    return extendedCode;
  }

  public void setExtendedCode(String extendedCode) {
    this.extendedCode = extendedCode;
  }
}
