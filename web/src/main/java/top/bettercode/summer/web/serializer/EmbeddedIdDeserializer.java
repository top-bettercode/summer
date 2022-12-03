package top.bettercode.summer.web.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.summer.web.serializer.annotation.JsonEmbeddedId;
import top.bettercode.summer.web.support.EmbeddedIdConverter;

/**
 * @author Peter Wu
 */
public class EmbeddedIdDeserializer extends JsonDeserializer<Object> implements
    ContextualDeserializer {

  private final Logger log = LoggerFactory.getLogger(EmbeddedIdDeserializer.class);
  private final String delimiter;


  public EmbeddedIdDeserializer() {
    this(EmbeddedIdConverter.DELIMITER);
  }

  public EmbeddedIdDeserializer(String delimiter) {
    this.delimiter = delimiter;
  }

  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    try {
      Object currentValue = p.getCurrentValue();
      String currentName = p.getCurrentName();
      Class<?> targetType = currentValue.getClass().getDeclaredField(currentName).getType();
      String src = p.getValueAsString();
      return EmbeddedIdConverter.toEmbeddedId(src, delimiter, targetType);
    } catch (NoSuchFieldException e) {
      log.warn("反序列化失败", e);
    }
    return null;
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
      throws JsonMappingException {
    String delimiter;
    if (property != null) {
      JsonEmbeddedId annotation = property.getAnnotation(JsonEmbeddedId.class);
      delimiter = annotation.value();
      delimiter = "".equals(delimiter) ? EmbeddedIdConverter.DELIMITER : delimiter;
    } else {
      delimiter = EmbeddedIdConverter.DELIMITER;
    }
    return new EmbeddedIdDeserializer(delimiter);
  }


}
