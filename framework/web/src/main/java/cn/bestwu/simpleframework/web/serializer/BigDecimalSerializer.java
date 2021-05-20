package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.web.serializer.annotation.JsonBigDecimal;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@JacksonStdImpl
public class BigDecimalSerializer extends NumberSerializer implements
    ContextualSerializer {

  private static final long serialVersionUID = -6196337598040684558L;
  private final int scale;

  public BigDecimalSerializer() {
    this(2);
  }

  public BigDecimalSerializer(int scale) {
    super(BigDecimal.class);
    this.scale = scale;
  }


  @Override
  public void serialize(Number value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    BigDecimal content = (BigDecimal) value;
    if (content.scale() != scale) {
      content = content.setScale(scale, RoundingMode.HALF_UP);
    }
    gen.writeNumber(content);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    if (property != null) {
      JsonBigDecimal annotation = property.getAnnotation(JsonBigDecimal.class);
      if (annotation == null) {
        throw new RuntimeException("未注解@" + JsonBigDecimal.class.getName());
      }
      return new BigDecimalSerializer(annotation.scale());
    }
    return this;
  }

}