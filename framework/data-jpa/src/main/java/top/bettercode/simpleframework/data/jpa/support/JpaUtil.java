package top.bettercode.simpleframework.data.jpa.support;

import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptorRegistry;
import org.hibernate.type.spi.TypeConfiguration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;

/**
 * @author Peter Wu
 */
public class JpaUtil {

  private static final JavaTypeDescriptorRegistry TYPE_DESCRIPTOR_REGISTRY = new TypeConfiguration().getJavaTypeDescriptorRegistry();
  private static final DefaultConversionService CONVERSION_SERVICE = new DefaultConversionService();

  @SuppressWarnings("unchecked")
  public static <T> T convert(Object source, Class<T> targetType) {
    if (source != null && !targetType.isInstance(source)) {
      JavaTypeDescriptor<T> descriptor = (JavaTypeDescriptor<T>) TYPE_DESCRIPTOR_REGISTRY.getDescriptor(
          ClassUtils.resolvePrimitiveIfNecessary(targetType));
      try {
        return descriptor.wrap(source, null);
      } catch (Exception e) {
        return CONVERSION_SERVICE.convert(source, targetType);
      }
    } else {
      return (T) source;
    }
  }

}
