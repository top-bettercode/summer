package top.bettercode.summer.tools.ctp.mdapi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "summer.ctp.mdapi", name = "register-front")
@EnableConfigurationProperties(MdapiProperties.class)
public class MdapiConfiguration {

  public MdapiConfiguration(MdapiProperties ctpMdapiProperties) throws Exception {
    MdapiNativeLibLoader.loadNativeLib();
  }

}
