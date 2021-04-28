package cn.bestwu.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@JacksonStdImpl
public class BigDecimalSerializer extends NumberSerializer {

  private static final long serialVersionUID = -6196337598040684558L;
  private static int newScale = 2;

  public BigDecimalSerializer() {
    super(BigDecimal.class);
  }

  public static void setNewScale(int newScale) {
    BigDecimalSerializer.newScale = newScale;
  }

  @Override
  public void serialize(Number value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    BigDecimal content = (BigDecimal) value;
    if (content.scale() < newScale) {
      content = content.setScale(newScale, RoundingMode.HALF_UP);
    }
    gen.writeNumber(content);
  }


}