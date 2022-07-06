package top.bettercode.simpleframework.config;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.cache.caffeine")
public class CaffeineCacheProperties {

  /**
   * 存储缓存配置,key:name ,value:spec {@link CaffeineSpec}
   */
  private Map<String, String> caches;
  /**
   * 默认缓存配置
   */
  private String defaultSpec = "maximumSize=1000,expireAfterAccess=10s";

  public Map<String, String> getCaches() {
    return caches;
  }

  public CaffeineCacheProperties setCaches(Map<String, String> caches) {
    this.caches = caches;
    return this;
  }

  public String getDefaultSpec() {
    return defaultSpec;
  }

  public CaffeineCacheProperties setDefaultSpec(String defaultSpec) {
    this.defaultSpec = defaultSpec;
    return this;
  }
}
