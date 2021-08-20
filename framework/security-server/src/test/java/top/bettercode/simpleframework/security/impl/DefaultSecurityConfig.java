package top.bettercode.simpleframework.security.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.security.server.IRevokeTokenService;

@ConditionalOnWebApplication
//@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class DefaultSecurityConfig {

  @Bean
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeRequests(authorizeRequests ->
            authorizeRequests.anyRequest().permitAll()
        )
        .csrf().disable();
    return http.build();
  }


  @Bean
  public IRevokeTokenService revokeTokenService() {
    return (securityUser) -> System.err.println("revoke:"+StringUtil.valueOf(securityUser, true));
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return NoOpPasswordEncoder.getInstance();
  }

}