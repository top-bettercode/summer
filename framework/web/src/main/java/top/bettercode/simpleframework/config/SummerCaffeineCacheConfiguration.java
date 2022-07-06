package top.bettercode.simpleframework.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CaffeineCache 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(CacheManager.class)
@AutoConfigureBefore(name = "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration")
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class SummerCaffeineCacheConfiguration {

  @Bean
  public CacheManager cacheManager(CaffeineCacheProperties caffeineCacheProperties) {
    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
    Map<String, String> caches = caffeineCacheProperties.getCaches();
    if (caches != null) {
      caches.forEach((name, spec) -> caffeineCacheManager.registerCustomCache(name,
          Caffeine.from(spec).build()));
    } else {
      caffeineCacheManager.setCacheSpecification(caffeineCacheProperties.getDefaultSpec());
    }
    return caffeineCacheManager;
  }
}
