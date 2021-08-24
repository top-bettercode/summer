package top.bettercode.simpleframework.security.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.security.IResourceService;
import top.bettercode.simpleframework.security.IRevokeTokenService;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class TestSecurityConfiguration {

  @Service
  public static class ApiServiceImpl implements IResourceService {
  }

  @Bean
  public IRevokeTokenService revokeTokenService() {
    return (securityUser) -> System.err
        .println(StringUtil.valueOf(securityUser, true));
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder(){

      @Override
      public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
      }

      @Override
      public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.toString().equals(encodedPassword);
      }
    };
  }

}