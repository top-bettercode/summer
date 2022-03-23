package top.bettercode.simpleframework.data.jpa.support;

import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;

/**
 * @author Peter Wu
 */
@SuppressWarnings("deprecation")
public class JpaUtil {

  private static final org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry TYPE_DESCRIPTOR_REGISTRY = org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry.INSTANCE;
  private static final DefaultConversionService CONVERSION_SERVICE = new DefaultConversionService();

  @SuppressWarnings({"unchecked"})
  public static <T> T convert(Object source, Class<T> targetType) {
    if (source != null && !targetType.isInstance(source)) {
      JavaTypeDescriptor<T> descriptor = (JavaTypeDescriptor<T>) TYPE_DESCRIPTOR_REGISTRY.getDescriptor(
          ClassUtils.resolvePrimitiveIfNecessary(targetType));
      try {
        if (descriptor instanceof org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry.FallbackJavaTypeDescriptor) {
          return CONVERSION_SERVICE.convert(source, targetType);
        } else {
          return descriptor.wrap(source, null);
        }
      } catch (Exception e) {
        return CONVERSION_SERVICE.convert(source, targetType);
      }
    } else {
      return (T) source;
    }
  }

}
