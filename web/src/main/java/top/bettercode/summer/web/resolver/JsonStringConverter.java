package top.bettercode.summer.web.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 * @since 0.0.1
 */
public class JsonStringConverter implements ConditionalGenericConverter {

  private final Logger log = LoggerFactory.getLogger(JsonStringConverter.class);
  private final ObjectMapper objectMapper;

  public JsonStringConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean matches(@NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
    return true;
  }

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
  }

  @Override
  public Object convert(Object source, @NotNull TypeDescriptor sourceType, @NotNull TypeDescriptor targetType) {
    if (!StringUtils.hasText((String) source)) {
      return null;
    }
    CollectionType collectionType = TypeFactory
        .defaultInstance().constructCollectionType(List.class, targetType.getResolvableType().resolveGeneric(0));
    try {
      return objectMapper.readValue((String) source, collectionType);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
