package cn.bestwu.simpleframework.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@ConditionalOnProperty(prefix = "summer.security.cors", value = "enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfiguration {

  @Bean("corsConfigurationSource")
  public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration(corsProperties.getPath(), corsProperties);
    return source;
  }

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  public CorsFilter corsFilter(
      @Qualifier("corsConfigurationSource") CorsConfigurationSource configurationSource) {
    return new CorsFilter(configurationSource);
  }

}
