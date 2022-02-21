package top.bettercode.simpleframework.support;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

/**
 * @author Peter Wu
 */
public class EmbeddedIdConverter {

  private static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();

  public static <T> String toString(T embeddedId) {
    Class<?> clazz = embeddedId.getClass();
    return Arrays.stream(BeanUtils.getPropertyDescriptors(clazz)).filter(
            o -> !"class".equals(o.getName()) && o.getReadMethod() != null
                && o.getWriteMethod() != null).sorted(
            Comparator.comparing(FeatureDescriptor::getName))
        .map(o -> CONVERSION_SERVICE.convert(
            ReflectionUtils.invokeMethod(o.getReadMethod(), embeddedId), String.class))
        .collect(Collectors.joining("+"));
  }

  public static <T> T toEmbeddedId(String src, Class<T> type) {
    String[] values = src.split("\\+");
    T result = BeanUtils.instantiateClass(type);
    List<PropertyDescriptor> descriptors = Arrays.stream(BeanUtils.getPropertyDescriptors(type))
        .filter(o -> !"class".equals(o.getName()) && o.getReadMethod() != null
            && o.getWriteMethod() != null).sorted(
            Comparator.comparing(FeatureDescriptor::getName)).collect(Collectors.toList());
    for (int i = 0; i < descriptors.size(); i++) {
      PropertyDescriptor descriptor = descriptors.get(i);
      ReflectionUtils.invokeMethod(
          descriptor.getWriteMethod(), result,
          CONVERSION_SERVICE.convert(values[i], descriptor.getPropertyType()));
    }
    return result;
  }


}
