package top.bettercode.summer.tools.sap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("jco.client")
public class SapProperties {
  private String ashost;
  private String sysnr;
  private String client;
  private String user;
  private String passwd;
  private String lang;
  private String poolCapacity;
  private String peakLimit;

  public String getAshost() {
    return ashost;
  }

  public void setAshost(String ashost) {
    this.ashost = ashost;
  }

  public String getSysnr() {
    return sysnr;
  }

  public void setSysnr(String sysnr) {
    this.sysnr = sysnr;
  }

  public String getClient() {
    return client;
  }

  public void setClient(String client) {
    this.client = client;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPasswd() {
    return passwd;
  }

  public void setPasswd(String passwd) {
    this.passwd = passwd;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getPoolCapacity() {
    return poolCapacity;
  }

  public void setPoolCapacity(String poolCapacity) {
    this.poolCapacity = poolCapacity;
  }

  public String getPeakLimit() {
    return peakLimit;
  }

  public void setPeakLimit(String peakLimit) {
    this.peakLimit = peakLimit;
  }
}
