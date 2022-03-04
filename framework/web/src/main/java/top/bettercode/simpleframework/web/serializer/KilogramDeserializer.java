package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import top.bettercode.simpleframework.support.KilogramUtil;

/**
 * @author Peter Wu
 */
public class KilogramDeserializer extends JsonDeserializer<Long> {

  @Override
  public Long deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    return KilogramUtil.toGram(p.getValueAsString());
  }
}
