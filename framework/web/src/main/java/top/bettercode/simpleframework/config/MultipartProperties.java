package top.bettercode.simpleframework.config;

import kotlin.text.StringsKt;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.bettercode.lang.util.ArrayUtil;

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

  /**
   * 文件资源访问位置
   */
  private String[] staticLocations;

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

  public String[] getStaticLocations() {
    return ArrayUtil.isNotEmpty(staticLocations) ? staticLocations
        : new String[]{
            "file:" + (baseSavePath.endsWith("static") ? StringsKt.substringBeforeLast(baseSavePath,
                "static", baseSavePath) : baseSavePath)};
  }

  public void setStaticLocations(String[] staticLocations) {
    this.staticLocations = staticLocations;
  }
}
