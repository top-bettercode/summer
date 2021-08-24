package top.bettercode.simpleframework.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Peter Wu
 */
public interface ScopeUserDetailsService extends UserDetailsService {

  default UserDetails loadUserByScopeAndUsername(String scope, String username)
      throws UsernameNotFoundException {
    return loadUserByUsername(username);
  }

}
