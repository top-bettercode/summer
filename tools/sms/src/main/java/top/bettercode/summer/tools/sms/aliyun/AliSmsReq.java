package top.bettercode.summer.tools.sms.aliyun;

import java.util.Map;

/**
 * @author Peter Wu
 */
public class AliSmsReq {

  /**
   * 接收短信的手机号码
   */
  private String phoneNumber;
  /**
   * 短信签名名称
   */
  private String signName;
  /**
   * 短信模板变量对应的实际值
   */
  private Map<String, String> templateParam;

  public AliSmsReq() {
  }

  public AliSmsReq(String phoneNumber, String signName,
      Map<String, String> templateParam) {
    this.phoneNumber = phoneNumber;
    this.signName = signName;
    this.templateParam = templateParam;
  }

  //--------------------------------------------

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getSignName() {
    return signName;
  }

  public void setSignName(String signName) {
    this.signName = signName;
  }

  public Map<String, String> getTemplateParam() {
    return templateParam;
  }

  public void setTemplateParam(Map<String, String> templateParam) {
    this.templateParam = templateParam;
  }

  //--------------------------------------------

  public static AliSmsReq of(String phoneNumber, String signName,
      Map<String, String> templateParam) {
    return new AliSmsReq(phoneNumber, signName, templateParam);
  }
}
