package cn.bestwu.simpleframework.security.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@ConditionalOnWebApplication
public class SecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Configuration
  @ConditionalOnWebApplication
  protected static class AuthenticationSecurity extends GlobalAuthenticationConfigurerAdapter {

    private final UserDetailsService userDetailsService;


    @Autowired
    public AuthenticationSecurity(UserDetailsService userDetailsService) {
      this.userDetailsService = userDetailsService;
    }

    /**
     * 自定义UserDetailsService
     *
     * @param auth auth
     * @throws Exception Exception
     */
    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService(userDetailsService);
    }

  }
}