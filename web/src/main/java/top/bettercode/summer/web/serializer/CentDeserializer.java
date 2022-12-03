package top.bettercode.summer.web.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import top.bettercode.summer.tools.lang.util.MoneyUtil;

/**
 * @author Peter Wu
 */
public class CentDeserializer extends JsonDeserializer<Long> {

  @Override
  public Long deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    return MoneyUtil.toCent(p.getValueAsString());
  }
}
