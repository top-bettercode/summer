package top.bettercode.summer.web.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.summer.web.support.packagescan.PackageScanClassResolver;

/**
 * @author Peter Wu
 * @since 0.1.14
 */
@AutoConfigureBefore({JacksonAutoConfiguration.class})
@Configuration(proxyBeanMethods = false)
public class WebConfiguration {

  @Bean
  public PackageScanClassResolver packageScanClassResolver(ApplicationContext applicationContext) {
    return new PackageScanClassResolver(applicationContext.getClassLoader());
  }

}
