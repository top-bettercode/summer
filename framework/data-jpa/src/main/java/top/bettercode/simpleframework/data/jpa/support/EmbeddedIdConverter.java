package top.bettercode.simpleframework.data.jpa.support;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import javax.persistence.Embeddable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.StringUtils;
import top.bettercode.lang.util.StringUtil;

public class EmbeddedIdConverter implements ConditionalGenericConverter {

  @Override
  public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
    return AnnotatedElementUtils.isAnnotated(targetType.getType(), Embeddable.class)
        && sourceType.getType() == String.class;
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
    Class<?> type = targetType.getType();
    return StringUtil.readJson(source.getBytes(StandardCharsets.UTF_8), type);
  }

}
