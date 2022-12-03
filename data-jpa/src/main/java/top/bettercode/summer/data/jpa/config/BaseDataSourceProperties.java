package top.bettercode.summer.data.jpa.config;

import com.zaxxer.hikari.HikariDataSource;

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
  /**
   * Locations of MyBatis mapper files.
   */
  private String[] mapperLocations;

  private HikariDataSource hikari;

  //--------------------------------------------
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

  public String[] getMapperLocations() {
    return mapperLocations;
  }

  public void setMapperLocations(String[] mapperLocations) {
    this.mapperLocations = mapperLocations;
  }

  public HikariDataSource getHikari() {
    return hikari;
  }

  public void setHikari(HikariDataSource hikari) {
    this.hikari = hikari;
  }
}