package top.bettercode.simpleframework.security;

import java.util.Set;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * @author Peter Wu
 */
public class AuthorityAuthorizationManagerExt {

  private AuthorizationManager<RequestAuthorizationContext> manager;
  private final Set<String> authorities;

  public AuthorityAuthorizationManagerExt(AuthorizationManager<RequestAuthorizationContext> manager,
      Set<String> authorities) {
    this.manager = manager;
    this.authorities = authorities;
  }

  public AuthorizationManager<RequestAuthorizationContext> getManager() {
    return manager;
  }

  public void setManager(
      AuthorizationManager<RequestAuthorizationContext> manager) {
    this.manager = manager;
  }

  public Set<String> getAuthorities() {
    return authorities;
  }
}
