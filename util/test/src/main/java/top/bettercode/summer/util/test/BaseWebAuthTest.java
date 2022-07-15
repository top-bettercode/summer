package top.bettercode.summer.util.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import top.bettercode.simpleframework.security.ScopeUserDetailsService;
import top.bettercode.simpleframework.security.UserDetailsAuthenticationToken;

/**
 * mockMvc 基础测试类
 *
 * @author Peter Wu
 */
public abstract class BaseWebAuthTest extends BaseWebNoAuthTest {

  protected String username = "root";
  protected String scope = null;
  @Autowired
  UserDetailsService userDetailsService;

  @Override
  public void defaultBeforeEach() throws Exception {
    beforeEach();
    UserDetails userDetails;
    if (scope != null && userDetailsService instanceof ScopeUserDetailsService) {
      userDetails = ((ScopeUserDetailsService) userDetailsService).loadUserByScopeAndUsername(scope,
          username);
    } else {
      userDetails = userDetailsService.loadUserByUsername(username);
    }
    SecurityContextHolder.getContext()
        .setAuthentication(new UserDetailsAuthenticationToken(userDetails));
    System.err.println("------------------------------------------------------");
  }


}
