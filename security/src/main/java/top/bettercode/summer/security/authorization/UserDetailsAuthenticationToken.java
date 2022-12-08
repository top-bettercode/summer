package top.bettercode.summer.security.authorization;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Peter Wu
 */
public class UserDetailsAuthenticationToken extends AbstractAuthenticationToken {

  private static final long serialVersionUID = 1L;
  private final UserDetails userDetails;

  public UserDetailsAuthenticationToken(UserDetails userDetails) {
    super(userDetails.getAuthorities());
    this.userDetails = userDetails;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return "N/A";
  }

  @Override
  public Object getPrincipal() {
    return this.userDetails;
  }
}
