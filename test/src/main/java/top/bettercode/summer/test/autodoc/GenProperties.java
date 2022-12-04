package top.bettercode.summer.test.autodoc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import top.bettercode.summer.tools.autodoc.AutodocExtension;
import top.bettercode.summer.tools.generator.DataType;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.autodoc.gen")
public class GenProperties extends AutodocExtension {

  /**
   * 是否启用
   */
  private Boolean enable = true;
  /**
   * 异常时是否不生成文档
   */
  private Boolean disableOnException = true;

  /**
   * 数据源类型
   */
  private DataType dataType = DataType.DATABASE;
  /**
   * 项目路径
   */
  private String projectPath = "";

  /**
   * 表前缀
   */
  private String[] tablePrefixes = new String[0];
  /**
   * 表后缀
   */
  private String[] tableSuffixes = new String[0];
  /**
   * 实体前缀
   */
  private String entityPrefix = "";

  public Boolean getEnable() {
    return enable;
  }

  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  public Boolean getDisableOnException() {
    return disableOnException;
  }

  public void setDisableOnException(Boolean disableOnException) {
    this.disableOnException = disableOnException;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public String getProjectPath() {
    return projectPath;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }

  public String[] getTablePrefixes() {
    return tablePrefixes;
  }

  public void setTablePrefixes(String[] tablePrefixes) {
    this.tablePrefixes = tablePrefixes;
  }

  public String[] getTableSuffixes() {
    return tableSuffixes;
  }

  public void setTableSuffixes(String[] tableSuffixes) {
    this.tableSuffixes = tableSuffixes;
  }

  public String getEntityPrefix() {
    return entityPrefix;
  }

  public GenProperties setEntityPrefix(String entityPrefix) {
    this.entityPrefix = entityPrefix;
    return this;
  }
}
