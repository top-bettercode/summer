package top.bettercode.summer.security.userdetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Peter Wu
 */
public interface CheckedUserDetailsService extends UserDetailsService {

  void validate(UserDetails userDetails);

}
