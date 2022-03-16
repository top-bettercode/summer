package top.bettercode.simpleframework.data.jpa.support;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * @author Peter Wu
 */
public class JpaUtil {

  private static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();

  @SuppressWarnings("unchecked")
  public static <T> T convert(Object source, Class<T> targetType) {
    if (source != null && !source.getClass().equals(targetType)) {
      if (boolean.class.equals(targetType) || Boolean.class.equals(targetType)) {
        if ("1".equals(String.valueOf(source))) {
          source = true;
        } else if ("0".equals(String.valueOf(source))) {
          source = false;
        }
      }
      return JpaUtil.CONVERSION_SERVICE.convert(source, targetType);
    } else {
      return (T) source;
    }
  }

}
