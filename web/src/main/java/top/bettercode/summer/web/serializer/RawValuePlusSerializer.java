package top.bettercode.summer.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import java.io.IOException;
import org.springframework.util.StringUtils;

public class RawValuePlusSerializer<T> extends RawSerializer<T> {

  private static final long serialVersionUID = 1L;

  public RawValuePlusSerializer() {
    super(String.class);
  }

  @Override
  public void serialize(T value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    String content = value.toString();
    if (StringUtils.hasText(content)) {
      super.serialize(value, gen, provider);
    } else {
      gen.writeNull();
    }
  }


}
