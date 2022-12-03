package top.bettercode.summer.data.jpa.support;

import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.springframework.util.ClassUtils;

/**
 * @author Peter Wu
 */
@SuppressWarnings("deprecation")
public class JpaUtil {

  private static final org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry TYPE_DESCRIPTOR_REGISTRY = org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry.INSTANCE;

  @SuppressWarnings({"unchecked"})
  public static <T> T convert(Object source, Class<T> targetType) {
    if (source != null && !targetType.isInstance(source)) {
      JavaTypeDescriptor<T> descriptor = (JavaTypeDescriptor<T>) TYPE_DESCRIPTOR_REGISTRY.getDescriptor(
          ClassUtils.resolvePrimitiveIfNecessary(targetType));
      return descriptor.wrap(source, null);
    } else {
      return (T) source;
    }
  }

}
