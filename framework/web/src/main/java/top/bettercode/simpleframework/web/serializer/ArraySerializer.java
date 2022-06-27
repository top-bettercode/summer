package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.web.serializer.annotation.JsonArray;

/**
 * JSON序列化url自动补全
 *
 * @author Peter Wu
 */
@JacksonStdImpl
public class ArraySerializer extends StdScalarSerializer<String> implements
    ContextualSerializer {

  private static final long serialVersionUID = 1L;

  private final boolean useExtensionField;
  private final String separator;

  public ArraySerializer() {
    this(true, ",");
  }

  public ArraySerializer(
      boolean useExtensionField,
      String separator) {
    super(String.class, false);
    this.useExtensionField = useExtensionField;
    this.separator = separator;
  }


  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    if (useExtensionField) {
      gen.writeString(value);
      JsonStreamContext outputContext = gen.getOutputContext();
      String fieldName = outputContext.getCurrentName();
      gen.writeObjectField(fieldName + "Array",
          (StringUtils.hasText(value) ? value.split(separator) : new String[0]));
    } else {
      gen.writeObject(
          (StringUtils.hasText(value) ? value.split(separator) : new String[0]));
    }
  }


  @Override
  public final void serializeWithType(String value, JsonGenerator gen, SerializerProvider provider,
      TypeSerializer typeSer) throws IOException {
    serialize(value, gen, provider);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    if (property != null) {
      JsonArray annotation = property.getAnnotation(JsonArray.class);
      if (annotation == null) {
        throw new RuntimeException("未注解@" + JsonArray.class.getName());
      }
      return new ArraySerializer(annotation.extended(), annotation.value());
    }
    return this;
  }
}
