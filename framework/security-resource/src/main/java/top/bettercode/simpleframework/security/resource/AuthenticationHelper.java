package top.bettercode.simpleframework.security.resource;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames;

/**
 * @author Peter Wu
 */
public final class AuthenticationHelper {

  /**
   * @return 授权信息
   */
  private static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /**
   * @return 授权信息
   */
  public static Object getPrincipal() {
    Authentication authentication = getAuthentication();
    if (authentication != null) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof OAuth2User) {
        principal = ((OAuth2User) principal)
            .getAttribute(OAuth2IntrospectionClaimNames.SUBJECT);
      }
      return principal;
    }
    return null;
  }

  /**
   * @param authentication 授权信息
   * @param authority      权限
   * @return 授权信息是否包含指定权限
   */
  private static boolean hasAuthority(Authentication authentication, String authority) {
    for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
      if (grantedAuthority.getAuthority().equals(authority)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param authority 权限
   * @return 授权信息是否包含指定权限
   */
  public static boolean hasAuthority(String authority) {
    Authentication authentication = getAuthentication();
    if (authentication == null) {
      return false;
    }
    return hasAuthority(authentication, authority);
  }

}
