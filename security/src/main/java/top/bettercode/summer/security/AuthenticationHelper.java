package top.bettercode.summer.security;

import java.util.Collection;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
  public static UserDetails getPrincipal() {
    Authentication authentication = getAuthentication();
    if (authentication != null) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof UserDetails) {
        return (UserDetails) principal;
      }
    }
    return null;
  }

  public static Optional<String> getUsername() {
    Authentication authentication = getAuthentication();
    if (authentication != null) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof UserDetails) {
        return Optional.of(((UserDetails) principal).getUsername());
      } else {
        return principal == null ? Optional.empty() : Optional.of(String.valueOf(principal));
      }
    }
    return Optional.empty();
  }

  /**
   * @param authentication 授权信息
   * @param authority      权限
   * @return 授权信息是否包含指定权限
   */
  private static boolean hasAuthority(Authentication authentication, String authority) {
    return hasAuthority(authentication.getAuthorities(), authority);
  }

  public static boolean hasAuthority(Collection<? extends GrantedAuthority> authorities,
      String authority) {
    for (GrantedAuthority grantedAuthority : authorities) {
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
