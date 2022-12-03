package top.bettercode.summer.security;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Peter Wu
 */
public interface ScopeUserDetailsService extends UserDetailsService {

  UserDetails loadUserByScopeAndUsername(String scope, String username)
      throws UsernameNotFoundException;

  @Override
  default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = Objects.requireNonNull(requestAttributes).getRequest();
    String scope = request.getParameter(SecurityParameterNames.SCOPE);
    Assert.hasText(scope, "scope 不能为空");
    return loadUserByScopeAndUsername(scope, username);
  }
}
