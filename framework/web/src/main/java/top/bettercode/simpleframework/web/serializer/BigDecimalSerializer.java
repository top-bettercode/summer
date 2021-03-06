package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.web.serializer.annotation.JsonBigDecimal;

@JacksonStdImpl
public class BigDecimalSerializer extends StdScalarSerializer<BigDecimal> implements
    ContextualSerializer {

  private static final long serialVersionUID = -6196337598040684558L;
  private final int scale;
  private final boolean toPlainString;
  private final boolean reduceFraction;
  private final boolean percent;
  private final RoundingMode roundingMode;

  public BigDecimalSerializer() {
    this(2, RoundingMode.HALF_UP, false, false, false);
  }

  public BigDecimalSerializer(int scale, RoundingMode roundingMode, boolean toPlainString,
      boolean reduceFraction,
      boolean percent) {
    super(BigDecimal.class);
    this.scale = scale;
    this.roundingMode = roundingMode;
    this.toPlainString = toPlainString;
    this.reduceFraction = reduceFraction;
    this.percent = percent;
  }


  @Override
  public void serialize(BigDecimal value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    int scale = this.scale;
    BigDecimal content = value;
    if (scale == -1) {
      scale = content.scale();
    } else if (content.scale() != scale) {
      content = content.setScale(scale, roundingMode);
    }
    String plainString = content.toPlainString();
    if (reduceFraction) {
      plainString = reduceFraction(plainString);
      content = new BigDecimal(plainString);
    }
    if (toPlainString) {
      gen.writeString(plainString);
    } else {
      gen.writeNumber(content);
    }

    if (percent) {
      JsonStreamContext outputContext = gen.getOutputContext();
      String fieldName = outputContext.getCurrentName();
      String percentPlainSring = content.multiply(new BigDecimal(100))
          .setScale(scale - 2, roundingMode).toPlainString();
      if(reduceFraction){
        percentPlainSring=reduceFraction(percentPlainSring);
      }
      gen.writeStringField(fieldName + "Pct", percentPlainSring + "%");
    }
  }

  @NotNull
  private String reduceFraction(String plainString) {
    return plainString.contains(".") ? StringUtils
        .trimTrailingCharacter(StringUtils.trimTrailingCharacter(plainString, '0'), '.')
        : plainString;
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    if (property != null) {
      JsonBigDecimal annotation = property.getAnnotation(JsonBigDecimal.class);
      if (annotation == null) {
        throw new RuntimeException("?????????@" + JsonBigDecimal.class.getName());
      }
      return new BigDecimalSerializer(annotation.scale(), annotation.roundingMode(),
          annotation.toPlainString(),
          annotation.reduceFraction(), annotation.percent());
    }
    return this;
  }

}