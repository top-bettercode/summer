package top.bettercode.simpleframework.config;

import top.bettercode.simpleframework.support.packagescan.PackageScanClassResolver;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Peter Wu
 * @since 0.1.14
 */
@AutoConfigureBefore({JacksonAutoConfiguration.class})
@Configuration(proxyBeanMethods = false)
public class FrameworkConfiguration {

  @Bean
  public PackageScanClassResolver packageScanClassResolver(ApplicationContext applicationContext) {
    return new PackageScanClassResolver(applicationContext.getClassLoader());
  }
}
