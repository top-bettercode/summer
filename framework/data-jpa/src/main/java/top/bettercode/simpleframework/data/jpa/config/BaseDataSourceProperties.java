package top.bettercode.simpleframework.data.jpa.config;

import org.springframework.data.jpa.repository.EnableJpaExtRepositories;

public class BaseDataSourceProperties {

  /**
   * JDBC URL of the database.
   */
  private String url;

  /**
   * Login username of the database.
   */
  private String username;

  /**
   * Login password of the database.
   */
  private String password;

  /**
   * 配置 {@link EnableJpaExtRepositories @EnableJpaExtRepositories} 的类
   */
  private Class<?> extConfigClass;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Class<?> getExtConfigClass() {
    return extConfigClass;
  }

  public void setExtConfigClass(
      Class<?> extConfigClass) {
    this.extConfigClass = extConfigClass;
  }
}