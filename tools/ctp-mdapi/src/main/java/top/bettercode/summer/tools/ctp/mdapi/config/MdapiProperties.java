package top.bettercode.summer.tools.ctp.mdapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.ctp.mdapi")
public class MdapiProperties {

  /**
   * 地址
   */
  private String registerFront;
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
   * 是否使用UDP协议
   */
  private boolean udp;

  public String getRegisterFront() {
    return registerFront;
  }

  public void setRegisterFront(String registerFront) {
    this.registerFront = registerFront;
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

  public boolean isUdp() {
    return udp;
  }

  public void setUdp(boolean udp) {
    this.udp = udp;
  }
}
