package top.bettercode.summer.web.apisign;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.summer.web.apisign.ApiSignIgnore;

/**
 * 签名配置属性
 *
 * @author Peter Wu
 */
@ConfigurationProperties("summer.sign")
public class ApiSignProperties {

  /**
   * 验证签名时效时允许客户端与服务器的时差，单位秒 如果小于等于0 不验证签名时效.
   */
  private int allowableClientTimeDifference = 0;
  /**
   * 是否验证 userAgent.
   */
  private boolean verifyUserAgent = false;
  /**
   * 是否可跳过验证.
   */
  private boolean canSkip = true;
  /**
   * 需要验证签名的 Controller类名前缀.
   */
  private String[] handlerTypePrefix = {};
  /**
   * 签名参数名.
   */
  private String parameterName = "sign";
  /**
   * 默认客户端密码.
   */
  private String clientSecret;

  //--------------------------------------------
  public boolean isSimple() {
    return (!verifyUserAgent) && allowableClientTimeDifference <= 0;
  }

  public boolean requiredSign(Object handler) {
    if (!(handler instanceof HandlerMethod)) {
      return false;
    }
    HandlerMethod handlerMethod = (HandlerMethod) handler;
    if (handlerMethod.getBean() instanceof ErrorController) {
      return false;
    }
    if (handlerMethod.hasMethodAnnotation(ApiSignIgnore.class)) {
      return false;
    }
    Class<?> beanType = handlerMethod.getBeanType();
    if (beanType.isAnnotationPresent(ApiSignIgnore.class)) {
      return false;
    }

    String name = beanType.getName();
    for (String typePrefix : handlerTypePrefix) {
      if (name.matches("^" + typePrefix.replace(".", "\\.").replace("*", ".+") + ".*$")) {
        return true;
      }
    }
    return false;
  }

  //--------------------------------------------
  public String[] getHandlerTypePrefix() {
    return handlerTypePrefix;
  }

  public void setHandlerTypePrefix(String[] handlerTypePrefix) {
    this.handlerTypePrefix = handlerTypePrefix;
  }

  public int getAllowableClientTimeDifference() {
    return allowableClientTimeDifference;
  }

  public void setAllowableClientTimeDifference(int allowableClientTimeDifference) {
    this.allowableClientTimeDifference = allowableClientTimeDifference;
  }

  public boolean isVerifyUserAgent() {
    return verifyUserAgent;
  }

  public void setVerifyUserAgent(boolean verifyUserAgent) {
    this.verifyUserAgent = verifyUserAgent;
  }

  public boolean isCanSkip() {
    return canSkip;
  }

  public void setCanSkip(boolean canSkip) {
    this.canSkip = canSkip;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }
}
