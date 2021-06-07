package top.bettercode.simpleframework.security.impl;

import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.security.resource.IResourceService;
import top.bettercode.simpleframework.security.server.IRevokeTokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.stereotype.Service;

@Deprecated
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableAuthorizationServer
public class TestSecurityConfiguration {

  @Service
  public static class ApiServiceImpl implements IResourceService {
  }

  @Bean
  public IRevokeTokenService revokeTokenService() {
    return (securityUser, access_token) -> System.err
        .println(StringUtil.valueOf(securityUser, true));
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

}