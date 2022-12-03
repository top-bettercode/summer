package top.bettercode.summer.security;

import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Peter Wu
 */
public interface GrantTypeUserDetailsService extends UserDetailsService {
  UserDetails loadUserByGrantTypeAndRequest(String grantType, HttpServletRequest request)
      throws UsernameNotFoundException;

}
