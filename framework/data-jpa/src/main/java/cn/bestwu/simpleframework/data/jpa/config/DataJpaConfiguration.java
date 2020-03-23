package cn.bestwu.simpleframework.data.jpa.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@ImportAutoConfiguration(JpaMybatisAutoConfiguration.class)
@Configuration
public class DataJpaConfiguration {

  @Bean("jpaExtProperties")
  @ConditionalOnMissingBean
  @ConfigurationProperties("spring.data.jpa.ext")
  public JpaExtProperties jpaExtProperties() {
    return new JpaExtProperties();
  }
}
