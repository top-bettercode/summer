package top.bettercode.summer.security.userdetails;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Peter Wu
 */
public interface NeedKickedOutValidator {

  boolean validate(String scope, UserDetails userDetails);

}
