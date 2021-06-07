package top.bettercode.simpleframework.security.impl;

import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.security.server.IRevokeTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class TestSecurityConfiguration {

  @Bean
  public IRevokeTokenService revokeTokenService() {
    return (securityUser, access_token) -> System.err.println("revoke:"+StringUtil.valueOf(securityUser, true));
  }

  @Deprecated
  @Bean
  public PasswordEncoder passwordEncoder(){
    return NoOpPasswordEncoder.getInstance();
  }

}