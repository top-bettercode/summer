package top.bettercode.simpleframework.data.jpa.config;

import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.simpleframework.data.jpa.IbatisErrorHandler;

/**
 * DataJpaConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({MybatisProperties.class})
public class DataJpaAutoConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "summer.datasource.multi", name = "enabled")
  public MultiDatasourcesBeanDefinitionRegistryPostProcessor repositoryBeanDefinitionRegistryPostProcessor() {
    return new MultiDatasourcesBeanDefinitionRegistryPostProcessor();
  }

  @Bean("jpaExtProperties")
  @ConditionalOnMissingBean
  @ConfigurationProperties("spring.data.jpa.ext")
  public JpaExtProperties jpaExtProperties() {
    return new JpaExtProperties();
  }

  @ConditionalOnWebApplication
  @Bean
  public IbatisErrorHandler ibatisErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    return new IbatisErrorHandler(messageSource, request);
  }

}
