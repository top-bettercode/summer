package top.bettercode.summer.tools.sap.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.summer.tools.sap.connection.SapGenService;
import top.bettercode.summer.tools.sap.connection.SapService;

/**
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "jco.client", name = "ashost")
@EnableConfigurationProperties(SapProperties.class)
public class SapConfiguration {

  public SapConfiguration() throws Exception {
    JcoLoader.loadJco();
  }

  @Bean
  public SapService sapService(SapProperties sapProperties) {
    return new SapService(sapProperties);
  }


  @ConditionalOnClass(name = "top.bettercode.summer.tools.generator.GeneratorExtension")
  @Bean
  public SapGenService sapGenService(SapService sapService) {
    return new SapGenService(sapService);
  }
}
