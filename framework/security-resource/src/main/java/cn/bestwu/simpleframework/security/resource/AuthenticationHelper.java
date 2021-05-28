package cn.bestwu.simpleframework.security.resource;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Peter Wu
 */
public final class AuthenticationHelper {

  /**
   * @return 授权信息
   */
  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /**
   * @return 授权信息
   */
  public static Object getPrincipal() {
    Authentication authentication = getAuthentication();
    if (authentication != null) {
      return authentication.getPrincipal();
    }
    return null;
  }

  /**
   * @param authentication 授权信息
   * @param authority 权限
   * @return 授权信息是否包含指定权限
   */
  public static boolean hasAuthority(Authentication authentication, String authority) {
    for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
      if (grantedAuthority.getAuthority().equals(authority)) {
        return true;
      }
    }
    return false;
  }

}
