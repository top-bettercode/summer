package top.bettercode.simpleframework.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.simpleframework.support.code.ICodeService;
import top.bettercode.simpleframework.web.DicCodesEndpoint;

/**
 * @author Peter Wu
 */
@ConditionalOnClass(WebEndpointProperties.class)
@ConditionalOnBean(WebEndpointProperties.class)
@AutoConfigureAfter(org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
public class DicCodesEndpointConfiguration {

  @Bean
  public DicCodesEndpoint dicCodesEndpoint(ICodeService codeService) {
    return new DicCodesEndpoint(codeService);
  }

}
