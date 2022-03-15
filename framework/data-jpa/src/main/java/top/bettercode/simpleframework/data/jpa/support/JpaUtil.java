package top.bettercode.simpleframework.data.jpa.support;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * @author Peter Wu
 */
public class JpaUtil {
  public static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();
}
