package top.bettercode.simpleframework;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Peter Wu
 */
public class UserInfoHelper {

  private static final String KEY = UserInfoHelper.class.getName() + ".key";

  public static void put(HttpServletRequest request, Object userInfo) {
    request.setAttribute(KEY, userInfo);
  }

  public static Object get(HttpServletRequest request) {
    return request.getAttribute(KEY);
  }

}
