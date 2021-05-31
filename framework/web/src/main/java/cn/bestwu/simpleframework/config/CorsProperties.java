package cn.bestwu.simpleframework.config;

import java.util.Collections;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

/**
 * @author Peter Wu
 * @since 0.1.12
 */
@ConfigurationProperties("summer.security.cors")
public class CorsProperties extends CorsConfiguration {

  private boolean enable = true;
  private String path = "/**";

  public CorsProperties() {
    setAllowedOrigins(Collections.singletonList("*"));
    setAllowedMethods(Collections.singletonList("*"));
//    setAllowCredentials(true);
    setAllowedHeaders(Collections.singletonList("*"));
  }

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
