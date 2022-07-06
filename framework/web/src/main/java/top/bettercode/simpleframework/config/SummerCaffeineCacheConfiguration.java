package top.bettercode.simpleframework.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Map;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CaffeineCache 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class SummerCaffeineCacheConfiguration {

  @Bean
  public CacheManagerCustomizer<CaffeineCacheManager> caffeineCacheManagerCustomizer(
      CaffeineCacheProperties caffeineCacheProperties) {
    return cacheManager -> {
      Map<String, String> caches = caffeineCacheProperties.getCaches();
      if (caches != null) {
        caches.forEach((name, spec) -> cacheManager.registerCustomCache(name,
            Caffeine.from(spec).build()));
      } else {
        cacheManager.setCacheSpecification(caffeineCacheProperties.getDefaultSpec());
      }
    };

  }
}
