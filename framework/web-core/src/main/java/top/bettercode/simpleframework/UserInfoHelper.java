package top.bettercode.simpleframework;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

  public static Object get() {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (requestAttributes != null) {
      HttpServletRequest request = requestAttributes.getRequest();
      return get(request);
    }
    return null;
  }

}
