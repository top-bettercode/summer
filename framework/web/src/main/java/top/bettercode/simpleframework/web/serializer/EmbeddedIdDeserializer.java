package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.simpleframework.support.EmbeddedIdConverter;

/**
 * @author Peter Wu
 */
public class EmbeddedIdDeserializer extends JsonDeserializer<Object> {

  private final Logger log = LoggerFactory.getLogger(EmbeddedIdDeserializer.class);

  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    try {
      Object currentValue = p.getCurrentValue();
      String currentName = p.getCurrentName();
      Class<?> targetType = currentValue.getClass().getDeclaredField(currentName).getType();
      String src = p.getValueAsString();
      return EmbeddedIdConverter.toEmbeddedId(src,targetType);
    } catch (NoSuchFieldException e) {
      log.warn("反序列化失败", e);
    }
    return null;
  }
}
