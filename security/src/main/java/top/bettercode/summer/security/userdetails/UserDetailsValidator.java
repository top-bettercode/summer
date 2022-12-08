package top.bettercode.summer.security.userdetails;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Peter Wu
 */
public interface UserDetailsValidator {

  void validate(UserDetails userDetails);

}
