package cn.bestwu.simpleframework.web.resolver;

import cn.bestwu.lang.util.MoneyUtil;
import java.util.Collections;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.StringUtils;

public class YuanToCentConverter implements ConditionalGenericConverter {

  @Override
  public boolean matches(TypeDescriptor typeDescriptor, TypeDescriptor typeDescriptor1) {
    return typeDescriptor1.hasAnnotation(YuanToCent.class);
  }

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    return Collections.singleton(new ConvertiblePair(String.class, Long.class));
  }

  @Override
  public Object convert(Object object, TypeDescriptor sourceType, TypeDescriptor targetType) {
    if (!StringUtils.hasText((String) object)) {
      return null;
    }
    return MoneyUtil.toCent((String) object);
  }

}
