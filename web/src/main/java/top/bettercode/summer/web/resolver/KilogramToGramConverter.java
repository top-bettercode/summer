package top.bettercode.summer.web.resolver;

import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.StringUtils;
import top.bettercode.summer.web.support.KilogramUtil;

public class KilogramToGramConverter implements ConditionalGenericConverter {

  @Override
  public boolean matches(@NotNull TypeDescriptor typeDescriptor, TypeDescriptor typeDescriptor1) {
    return typeDescriptor1.hasAnnotation(KilogramToGram.class);
  }

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    return Collections.singleton(new ConvertiblePair(String.class, Long.class));
  }

  @Override
  public Object convert(Object object, @NotNull TypeDescriptor sourceType,
      @NotNull TypeDescriptor targetType) {
    if (!StringUtils.hasText((String) object)) {
      return null;
    }
    return KilogramUtil.toGram((String) object);
  }
}
