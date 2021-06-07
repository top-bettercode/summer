package top.bettercode.simpleframework.data.jpa.support;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import javax.persistence.Embeddable;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.StringUtils;

public class EmbeddedIdConverter implements ConditionalGenericConverter {

  @Override
  public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
    return targetType.hasAnnotation(Embeddable.class) && sourceType.getType() == String.class;
  }

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    return Collections.singleton(new ConvertiblePair(String.class, Serializable.class));
  }

  @Override
  public Object convert(Object object, TypeDescriptor sourceType, TypeDescriptor targetType) {
    String source = (String) object;
    if (!StringUtils.hasText(source)) {
      return null;
    }
    try {
      return targetType.getType().getConstructor(String.class).newInstance(source);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

}
