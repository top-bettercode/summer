package top.bettercode.simpleframework.web.resolver;

import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.StringUtils;
import top.bettercode.lang.util.MoneyUtil;

/**
 * 原字符串(1.2)×100转长整型(120)注解
 */
public class CentConverter implements ConditionalGenericConverter {

  @Override
  public boolean matches(@NotNull TypeDescriptor sourceType, TypeDescriptor targetType) {
    return targetType.hasAnnotation(Cent.class);
  }

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    return Collections.singleton(new ConvertiblePair(String.class, Long.class));
  }

  @Override
  public Object convert(Object object, @NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
    if (!StringUtils.hasText((String) object)) {
      return null;
    }
    return MoneyUtil.toCent((String) object);
  }

}
