package top.bettercode.summer.web.support;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Peter Wu
 */
public class EmbeddedIdConverter {

  private static final ConcurrentMap<Class<?>, List<PropertyDescriptor>> cache = new ConcurrentHashMap<>();
  public static final String DELIMITER = ",";

  public static <T> String toString(T embeddedId) {
    return toString(embeddedId, DELIMITER);
  }

  public static <T> String toString(T embeddedId, String delimiter) {
    Class<?> clazz = embeddedId.getClass();
    return getPropertyDescriptors(clazz).stream()
        .map(o -> ApplicationContextHolder.getConversionService().convert(
            ReflectionUtils.invokeMethod(o.getReadMethod(), embeddedId), String.class))
        .collect(Collectors.joining(delimiter));
  }

  @NotNull
  private static List<PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {
    return cache.computeIfAbsent(clazz, c -> Arrays.stream(BeanUtils.getPropertyDescriptors(c))
        .filter(o -> !"class".equals(o.getName()) && o.getReadMethod() != null
            && o.getWriteMethod() != null).sorted(
            Comparator.comparing(FeatureDescriptor::getName)).collect(Collectors.toList()));
  }

  public static <T> T toEmbeddedId(String src, Class<T> type) {
    return toEmbeddedId(src, DELIMITER, type);
  }

  public static <T> T toEmbeddedId(String src, String delimiter, Class<T> type) {
    String[] values = Pattern.compile(delimiter).split(src);
    T result = BeanUtils.instantiateClass(type);
    List<PropertyDescriptor> descriptors = getPropertyDescriptors(type);
    for (int i = 0; i < descriptors.size(); i++) {
      PropertyDescriptor descriptor = descriptors.get(i);
      ReflectionUtils.invokeMethod(
          descriptor.getWriteMethod(), result,
          ApplicationContextHolder.getConversionService().convert(values[i], descriptor.getPropertyType()));
    }
    return result;
  }

}
