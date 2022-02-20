package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * @author Peter Wu
 */
public class EmbeddedIdDeserializer extends JsonDeserializer<Serializable> {

  private final Logger log = LoggerFactory.getLogger(EmbeddedIdDeserializer.class);

  @Override
  public Serializable deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    try {
      Object currentValue = p.getCurrentValue();
      String currentName = p.getCurrentName();
      Class<?> targetType = currentValue.getClass().getDeclaredField(currentName).getType();
      String valueAsString = p.getValueAsString();
      return (Serializable) BeanUtils.instantiateClass(targetType.getConstructor(String.class),
          valueAsString);
    } catch (NoSuchFieldException | NoSuchMethodException e) {
      log.warn("反序列化失败", e);
    }
    return null;
  }
}
