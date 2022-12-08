package top.bettercode.summer.security.userdetails;

import javax.servlet.http.HttpServletRequest;
import top.bettercode.summer.security.token.ApiToken;

/**
 * @author Peter Wu
 */
public interface LoginListener {

  default void beforeLogin(HttpServletRequest request, String grantType, String scope) {
  }

  default void afterLogin(ApiToken apiToken) {
  }

}
