package top.bettercode.simpleframework.security.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.security.IResourceService;
import top.bettercode.simpleframework.security.IRevokeTokenService;
import top.bettercode.simpleframework.security.authorization.RedisApiAuthorizationService;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
public class TestSecurityConfiguration {

  @Bean
  public RedisApiAuthorizationService redisApiAuthorizationService(
      RedisConnectionFactory redisConnectionFactory) {
    return new RedisApiAuthorizationService(redisConnectionFactory, "test");
  }

//  @Bean
//  public JdbcApiAuthorizationService jdbcApiAuthorizationService(
//      DataSource dataSource) {
//    return new JdbcApiAuthorizationService(dataSource);
//  }


  @Bean
  public IResourceService resourceService() {
    return new IResourceService() {
    };
  }

  @Bean
  public IRevokeTokenService revokeTokenService() {
    return (securityUser) -> System.err
        .println(StringUtil.valueOf(securityUser, true));
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder() {

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