package cn.bestwu.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointProperties;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the {@link WritableEnvironmentEndpoint}.
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingClass("org.springframework.cloud.autoconfigure.WritableEnvironmentEndpointAutoConfiguration")
@ConditionalOnClass({EnvironmentEndpoint.class, EnvironmentEndpointProperties.class})
@AutoConfigureBefore(EnvironmentEndpointAutoConfiguration.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@EnableConfigurationProperties({EnvironmentEndpointProperties.class})
public class WritableEnvironmentEndpointAutoConfiguration {

  private final EnvironmentEndpointProperties properties;

  public WritableEnvironmentEndpointAutoConfiguration(EnvironmentEndpointProperties properties) {
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean
  public EnvironmentManager environmentManager(ConfigurableEnvironment environment) {
    return new EnvironmentManager(environment);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnAvailableEndpoint
  public WritableEnvironmentEndpoint writableEnvironmentEndpoint(Environment environment) {
    WritableEnvironmentEndpoint endpoint = new WritableEnvironmentEndpoint(environment);
    String[] keysToSanitize = this.properties.getKeysToSanitize();
    if (keysToSanitize != null) {
      endpoint.setKeysToSanitize(keysToSanitize);
    }
    return endpoint;
  }

  @Bean
  @ConditionalOnAvailableEndpoint
  public WritableEnvironmentEndpointWebExtension writableEnvironmentEndpointWebExtension(
      WritableEnvironmentEndpoint endpoint, EnvironmentManager environment) {
    return new WritableEnvironmentEndpointWebExtension(endpoint, environment);
  }

}
