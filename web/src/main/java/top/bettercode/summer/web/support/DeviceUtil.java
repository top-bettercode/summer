package top.bettercode.summer.web.support;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 * 客户端设备工具
 *
 * @author Peter Wu
 */
public class DeviceUtil {

  /**
   * @param request request
   * @return UserAgent
   */
  public static String getUserAgent(HttpServletRequest request) {
    Enumeration<String> headers = request.getHeaders("user-agent");
    if (headers.hasMoreElements()) {
      return headers.nextElement();
    } else {
      return null;
    }
  }
}
