package top.bettercode.summer.tools.sms.b2m;

/**
 * <p>
 * { "mobile":"15538850000", "smsId":"20170392833833891100", "customSmsId":"20170392833833891100" }
 * </p>
 *
 * @author Peter Wu
 */
public class B2mRespData {

  private String mobile;
  /**
   * 平台消息ID
   */
  private String smsId;
  /**
   * 自定义消息ID
   */
  private String customSmsId;

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
}
