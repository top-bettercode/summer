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
import org.springframework.util.StringUtils;

@JacksonStdImpl
public class BigDecimalSerializer extends NumberSerializer implements
    ContextualSerializer {

  private static final long serialVersionUID = -6196337598040684558L;
  private final int scale;
  private final boolean toPlainString;
  private final boolean reduceFraction;

  public BigDecimalSerializer() {
    this(2, false, false);
  }

  public BigDecimalSerializer(int scale, boolean toPlainString, boolean reduceFraction) {
    super(BigDecimal.class);
    this.scale = scale;
    this.toPlainString = toPlainString;
    this.reduceFraction = reduceFraction;
  }


  @Override
  public void serialize(Number value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    BigDecimal content = (BigDecimal) value;
    if (content.scale() != scale) {
      content = content.setScale(scale, RoundingMode.HALF_UP);
    }
    if (toPlainString) {
      String plainString = content.toPlainString();
      if (reduceFraction) {
        plainString = plainString.contains(".") ? StringUtils
            .trimTrailingCharacter(StringUtils.trimTrailingCharacter(plainString, '0'), '.')
            : plainString;
      }
      gen.writeString(plainString);
    } else {
      gen.writeNumber(content);
    }
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    if (property != null) {
      JsonBigDecimal annotation = property.getAnnotation(JsonBigDecimal.class);
      if (annotation == null) {
        throw new RuntimeException("未注解@" + JsonBigDecimal.class.getName());
      }
      return new BigDecimalSerializer(annotation.scale(), annotation.toPlainString(),
          annotation.reduceFraction());
    }
    return this;
  }

}