package top.bettercode.summer.logging;

import java.util.Locale;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Peter Wu
 */
public class ApplicationContextHolder implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(@NotNull ApplicationContext applicationContext)
      throws BeansException {
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

  public static String getProperty(String key) {
    return applicationContext == null ? null : applicationContext.getEnvironment().getProperty(key);
  }


  public static String getProperty(String key, String defaultValue) {
    return applicationContext == null ? null
        : applicationContext.getEnvironment().getProperty(key, defaultValue);
  }


  public static <T> T getProperty(String key, Class<T> targetType) {
    return applicationContext == null ? null
        : applicationContext.getEnvironment().getProperty(key, targetType);
  }


  public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
    return applicationContext == null ? null
        : applicationContext.getEnvironment().getProperty(key, targetType, defaultValue);
  }

  public static ConversionService getConversionService() {
    return applicationContext == null ? new DefaultConversionService()
        : applicationContext.getBean(ConversionService.class);
  }
  public static Optional<ServletRequestAttributes> getRequestAttributes() {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    return Optional.ofNullable(requestAttributes);
  }
}
