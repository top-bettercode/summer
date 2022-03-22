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
      public LocalDate convert(Timestamp source) {
        return source.toLocalDateTime().toLocalDate();
      }
    });
    CONVERSION_SERVICE.addConverter(new Converter<Timestamp, LocalDateTime>() {
      @Override
      public LocalDateTime convert(Timestamp source) {
        return source.toLocalDateTime();
      }
    });
    CONVERSION_SERVICE.addConverter(new Converter<Boolean, Integer>() {
      @Override
      public Integer convert(Boolean source) {
        return source ? 1 : 0;
      }
    });
  }

  @SuppressWarnings("unchecked")
  public static <T> T convert(Object source, Class<T> targetType) {
    if (source != null && !source.getClass().equals(targetType)) {
      if (boolean.class.equals(targetType) || Boolean.class.equals(targetType)) {
        if ("1".equals(String.valueOf(source))) {
          return (T) Boolean.TRUE;
        } else if ("0".equals(String.valueOf(source))) {
          return (T) Boolean.FALSE;
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
