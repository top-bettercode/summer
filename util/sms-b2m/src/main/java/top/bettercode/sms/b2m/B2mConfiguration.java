package top.bettercode.sms.b2m;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统安全配置
 *
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(B2mProperties.class)
public class B2mConfiguration {

  private final Logger log = LoggerFactory.getLogger(B2mConfiguration.class);

  @Bean
  public B2mTemplate tmsService(B2mProperties b2mProperties) {
    return new B2mTemplate(b2mProperties);
  }

}