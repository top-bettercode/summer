package top.bettercode.simpleframework.support;

import java.util.Locale;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.NoSuchMessageException;

/**
 * @author Peter Wu
 */
public class ApplicationContextHolder implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ApplicationContextHolder.applicationContext = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public static <T> T getBean(String s, Class<T> aClass) throws BeansException {
    return applicationContext == null ? null : applicationContext.getBean(s, aClass);
  }

  public static <T> T getBean(Class<T> aClass) throws BeansException {
    return applicationContext == null ? null : applicationContext.getBean(aClass);
  }

  public static String getMessage(String s, Object[] objects, Locale locale)
      throws NoSuchMessageException {
    return applicationContext == null ? null : applicationContext.getMessage(s, objects, locale);
  }
}
