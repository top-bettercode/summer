package top.bettercode.autodoc.gen;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import top.bettercode.autodoc.core.AutodocExtension;
import top.bettercode.generator.DataType;
import top.bettercode.generator.JDBCConnectionConfiguration;

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
   * 在查询字段说明时，是否自动注入所有数据表
   */
  private Boolean allTables = true;
  /**
   * 是否生成文档
   */
  private Boolean doc = false;
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
   * JDBC连接配置
   */
  @NestedConfigurationProperty
  private JDBCConnectionConfiguration datasource = new JDBCConnectionConfiguration();

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

  public Boolean getAllTables() {
    return allTables;
  }

  public void setAllTables(Boolean allTables) {
    this.allTables = allTables;
  }

  public Boolean getDoc() {
    return doc;
  }

  public void setDoc(Boolean doc) {
    this.doc = doc;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public JDBCConnectionConfiguration getDatasource() {
    return datasource;
  }

  public void setDatasource(JDBCConnectionConfiguration datasource) {
    this.datasource = datasource;
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
}
