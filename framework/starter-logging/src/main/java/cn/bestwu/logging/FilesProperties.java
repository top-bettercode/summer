package cn.bestwu.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * logging file 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties("logging.files")
public class FilesProperties {

  private String path;
  private String maxFileSize = "100MB";
  private Integer maxHistory = 100;
  /**
   * 是否启动时滚动日志
   */
  private boolean rolloverOnStart = false;
  /**
   * 记录所有日志于一个文件
   */
  private boolean logAll = true;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(String maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  public Integer getMaxHistory() {
    return maxHistory;
  }

  public void setMaxHistory(Integer maxHistory) {
    this.maxHistory = maxHistory;
  }

  public boolean isRolloverOnStart() {
    return rolloverOnStart;
  }

  public void setRolloverOnStart(boolean rolloverOnStart) {
    this.rolloverOnStart = rolloverOnStart;
  }

  public boolean isLogAll() {
    return logAll;
  }

  public void setLogAll(boolean logAll) {
    this.logAll = logAll;
  }
}