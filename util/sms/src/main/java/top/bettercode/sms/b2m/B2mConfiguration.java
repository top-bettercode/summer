package top.bettercode.sms.b2m;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 亿美软通短信平台 配置
 *
 * @author Peter Wu
 */
@ConditionalOnProperty(prefix = "summer.sms.b2m", value = "app-id")
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(B2mProperties.class)
public class B2mConfiguration {

  private final Logger log = LoggerFactory.getLogger(B2mConfiguration.class);

  @Bean
  public B2mTemplate b2mTemplate(B2mProperties b2mProperties) {
    return new B2mTemplate(b2mProperties);
  }

}