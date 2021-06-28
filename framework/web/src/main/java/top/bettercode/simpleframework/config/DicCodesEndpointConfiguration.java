package top.bettercode.simpleframework.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.simpleframework.web.SettingsEndpoint;

/**
 * @author Peter Wu
 */
@ConditionalOnClass(WebEndpointProperties.class)
@ConditionalOnBean(WebEndpointProperties.class)
@AutoConfigureAfter(org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
public class DicCodesEndpointConfiguration {

  @Bean
  public SettingsEndpoint settingsEndpoint() {
    return new SettingsEndpoint();
  }

}
