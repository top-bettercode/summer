package top.bettercode.summer.tools.ctp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.ctp")
public class CtpProperties {

  /**
   * 行情地址
   */
  private String mdRegisterFront;
  /**
   * 交易地址
   */
  private String traderRegisterFront;
  /**
   * 期货公司的会员号
   */
  private String brokerId;
  /**
   * 投资者在该期货公司的客户号
   */
  private String userId;
  /**
   * 该投资者密码
   */
  private String password;
  /**
   * 产品标识
   */
  private String appId;
  /**
   * 产品授权码
   */
  private String authCode;
  /**
   * 货币
   */
  private String currencyId = "CNY";

  /**
   * 是否使用UDP协议
   */
  private boolean udp;

  public String getMdRegisterFront() {
    return mdRegisterFront;
  }

  public void setMdRegisterFront(String mdRegisterFront) {
    this.mdRegisterFront = mdRegisterFront;
  }

  public String getTraderRegisterFront() {
    return traderRegisterFront;
  }

  public void setTraderRegisterFront(String traderRegisterFront) {
    this.traderRegisterFront = traderRegisterFront;
  }

  public String getBrokerId() {
    return brokerId;
  }

  public void setBrokerId(String brokerId) {
    this.brokerId = brokerId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getAuthCode() {
    return authCode;
  }

  public void setAuthCode(String authCode) {
    this.authCode = authCode;
  }

  public String getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(String currencyId) {
    this.currencyId = currencyId;
  }

  public boolean isUdp() {
    return udp;
  }

  public void setUdp(boolean udp) {
    this.udp = udp;
  }
}
