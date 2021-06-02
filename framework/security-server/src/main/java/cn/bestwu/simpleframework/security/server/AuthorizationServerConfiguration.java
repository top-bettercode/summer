package cn.bestwu.simpleframework.security.server;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configuration.ClientDetailsServiceConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;

/**
 * OAuth2 服务器自动配置
 *
 * @author Peter Wu
 */
@Deprecated
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@Order(-1)
@ConditionalOnProperty(prefix = "summer.security.cors", value = "enable", havingValue = "true", matchIfMissing = true)
@Import({ClientDetailsServiceConfiguration.class, AuthorizationServerEndpointsConfiguration.class})
public class AuthorizationServerConfiguration extends AuthorizationServerSecurityConfiguration {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.cors();
  }
}
