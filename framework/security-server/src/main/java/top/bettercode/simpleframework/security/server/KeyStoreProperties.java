package top.bettercode.simpleframework.security.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.security.key-store")
public class KeyStoreProperties {

  private String alias;
  private String resourcePath;
  private String password;

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getResourcePath() {
    return resourcePath;
  }

  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
