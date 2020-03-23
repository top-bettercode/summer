package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.support.KilogramUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import java.io.IOException;
import java.math.BigDecimal;

@JacksonStdImpl
public class KilogramSerializer extends NumberSerializer {

  private static final long serialVersionUID = 6040177008446373376L;
  private static int newScale = 3;

  public KilogramSerializer() {
    super(Long.class);
  }

  public static void setNewScale(int newScale) {
    KilogramSerializer.newScale = newScale;
  }

  @Override
  public void serialize(Number value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    gen.writeString(KilogramUtil.toKilogram(new BigDecimal(value.toString()), newScale).toString());
  }

}