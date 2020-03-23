package cn.bestwu.simpleframework.support;

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
   * @return 客户端设备类型
   */
  public static String getDeviceInfo(HttpServletRequest request) {
    return getDeviceInfo(getUserAgent(request));
  }

  /**
   * @param user_agent user_agent
   * @return 客户端设备类型 1:android， 2:ios
   */
  public static String getDeviceInfo(String user_agent) {
    if (user_agent.indexOf("Android") > 0 || user_agent.indexOf("Commons-HttpClient") > 0) {
      return "1";
    } else if (user_agent.indexOf("iPhone") > 0) {
      return "2";
    }
    return "0";
  }

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
