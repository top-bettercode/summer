package cn.bestwu.autodoc.gen;

import cn.bestwu.autodoc.core.AutodocExtension;
import cn.bestwu.generator.DataType;
import cn.bestwu.generator.JDBCConnectionConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("autodoc.gen")
public class GenProperties extends AutodocExtension {

  /**
   * 是否启用
   */
  private Boolean enable = true;
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
  private String tablePrefix = "";
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

  public String getTablePrefix() {
    return tablePrefix;
  }

  public void setTablePrefix(String tablePrefix) {
    this.tablePrefix = tablePrefix;
  }
}
