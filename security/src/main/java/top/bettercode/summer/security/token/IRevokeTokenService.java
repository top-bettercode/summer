package top.bettercode.summer.security.token;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Peter Wu
 */
public interface IRevokeTokenService {

  void revokeToken(UserDetails principal);
}
