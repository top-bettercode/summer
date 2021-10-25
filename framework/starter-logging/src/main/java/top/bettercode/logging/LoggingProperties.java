package top.bettercode.logging;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.logging")
public class LoggingProperties {

  /**
   * 日志报警标题.
   */
  private String warnSubject = "${spring.application.name}  ${spring.profiles.active} system exception;";

  /**
   * Log levels severity mapping. Use 'root' for the root logger.Stored in different files,
   * respectively.
   */
  private Map<String, String> spilt;
  /**
   * Log levels severity mapping.Delimited files based on markup.
   */
  private Map<String, String> spiltMarker;

  /**
   * Log levels.Stored in different files, respectively.
   */
  private String[] spiltLevel;

  /**
   * 是否启用日志浏览功能.
   */
  private Boolean showEnabled = true;

  public String getWarnSubject() {
    return warnSubject;
  }

  public void setWarnSubject(String warnSubject) {
    this.warnSubject = warnSubject;
  }

  public Map<String, String> getSpilt() {
    return spilt;
  }

  public void setSpilt(Map<String, String> spilt) {
    this.spilt = spilt;
  }

  public Map<String, String> getSpiltMarker() {
    return spiltMarker;
  }

  public void setSpiltMarker(Map<String, String> spiltMarker) {
    this.spiltMarker = spiltMarker;
  }

  public String[] getSpiltLevel() {
    return spiltLevel;
  }

  public void setSpiltLevel(String[] spiltLevel) {
    this.spiltLevel = spiltLevel;
  }

  public Boolean getShowEnabled() {
    return showEnabled;
  }

  public void setShowEnabled(Boolean showEnabled) {
    this.showEnabled = showEnabled;
  }
}
