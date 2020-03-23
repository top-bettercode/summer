package cn.bestwu.simpleframework.web.kaptcha;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("kaptcha")
public class KaptchaProperties {

  private String border = "no";
  private String textproducerFontColor = "black";
  private int textproducerCharSpace = 5;
  /**
   * 验证码过期时间，单位秒
   */
  private int expireSeconds = 5 * 60;

  //--------------------------------------------

  public String getBorder() {
    return border;
  }

  public void setBorder(String border) {
    this.border = border;
  }

  public String getTextproducerFontColor() {
    return textproducerFontColor;
  }

  public void setTextproducerFontColor(String textproducerFontColor) {
    this.textproducerFontColor = textproducerFontColor;
  }

  public int getTextproducerCharSpace() {
    return textproducerCharSpace;
  }

  public void setTextproducerCharSpace(int textproducerCharSpace) {
    this.textproducerCharSpace = textproducerCharSpace;
  }

  public int getExpireSeconds() {
    return expireSeconds;
  }

  public void setExpireSeconds(int expireSeconds) {
    this.expireSeconds = expireSeconds;
  }
}
