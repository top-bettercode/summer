package cn.bestwu.simpleframework.security.impl;

import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.security.server.IRevokeTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@Configuration
@ConditionalOnWebApplication
@EnableResourceServer
public class TestSecurityConfiguration {

  @Bean
  public IRevokeTokenService revokeTokenService() {
    return (securityUser, access_token) -> System.err.println(StringUtil.valueOf(securityUser, true));
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return NoOpPasswordEncoder.getInstance();
  }

}