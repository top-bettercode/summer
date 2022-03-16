package top.bettercode.simpleframework.data.jpa.support;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * @author Peter Wu
 */
public class JpaUtil {

  private static final DefaultConversionService CONVERSION_SERVICE = new DefaultConversionService();

  static {
    CONVERSION_SERVICE.addConverter(new Converter<Timestamp, LocalDate>() {
      @Override
      public LocalDate convert(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toLocalDate();
      }
    });
    CONVERSION_SERVICE.addConverter(new Converter<Timestamp, LocalDateTime>() {
      @Override
      public LocalDateTime convert(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
      }
    });
  }

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

  public static boolean canConvert(Class<?> sourceType, Class<?> targetType) {
    return CONVERSION_SERVICE.canConvert(sourceType, targetType);
  }
}
