package top.bettercode.simpleframework.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public final class UserDetailsAuthenticationProvider implements AuthenticationProvider {


  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    authentication.setAuthenticated(true);
    return authentication;
  }


  @Override
  public boolean supports(Class<?> authentication) {
    return UserDetailsAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
