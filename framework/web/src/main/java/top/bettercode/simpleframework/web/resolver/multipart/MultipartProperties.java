package top.bettercode.simpleframework.web.resolver.multipart;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.multipart")
public class MultipartProperties {

  /**
   * 文件保存路径
   */
  private String baseSavePath;
  /**
   * 文件访问路径前缀
   */
  private String fileUrlFormat;
  /**
   * 保留原文件名
   */
  private boolean keepOriginalFilename = false;
  /**
   * 默认文件分类
   */
  private String defaultFileType = "file";

  //--------------------------------------------

  public String getBaseSavePath() {
    return baseSavePath;
  }

  public void setBaseSavePath(String baseSavePath) {
    this.baseSavePath = baseSavePath;
  }

  public String getFileUrlFormat() {
    return fileUrlFormat;
  }

  public void setFileUrlFormat(String fileUrlFormat) {
    this.fileUrlFormat = fileUrlFormat;
  }

  public boolean isKeepOriginalFilename() {
    return keepOriginalFilename;
  }

  public void setKeepOriginalFilename(boolean keepOriginalFilename) {
    this.keepOriginalFilename = keepOriginalFilename;
  }

  public String getDefaultFileType() {
    return defaultFileType;
  }

  public void setDefaultFileType(String defaultFileType) {
    this.defaultFileType = defaultFileType;
  }
}
