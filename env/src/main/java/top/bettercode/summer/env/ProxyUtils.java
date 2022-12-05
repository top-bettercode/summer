package top.bettercode.summer.env;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

/**
 * @author Ryan Baxter
 */
public final class ProxyUtils {

  private ProxyUtils() {
    throw new IllegalStateException("Can't instantiate a utility class");
  }

  @SuppressWarnings("unchecked")
  public static <T> T getTargetObject(Object candidate) {
    try {
      if (AopUtils.isAopProxy(candidate) && (candidate instanceof Advised)) {
        return (T) ((Advised) candidate).getTargetSource().getTarget();
      }
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to unwrap proxied object", ex);
    }
    return (T) candidate;
  }

}
