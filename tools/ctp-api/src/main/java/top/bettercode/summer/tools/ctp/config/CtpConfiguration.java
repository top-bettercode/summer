package top.bettercode.summer.tools.ctp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "summer.ctp", name = "broker-id")
@EnableConfigurationProperties(CtpProperties.class)
public class CtpConfiguration {

  public CtpConfiguration(CtpProperties ctpMdapiProperties) throws Exception {
    CtpNativeLibLoader.loadNativeLib();
  }

}
