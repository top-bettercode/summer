package top.bettercode.simpleframework.web.serializer;

import top.bettercode.simpleframework.web.serializer.annotation.JsonStringReplace;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
@JacksonStdImpl
public class StringReplaceSerializer extends StdScalarSerializer<String> implements
    ContextualSerializer {

  private static final long serialVersionUID = 1L;

  private final String target;
  private final String replacement;

  public StringReplaceSerializer() {
    super(String.class, false);
    target = "";
    replacement = "";
  }

  public StringReplaceSerializer(String target, String replacement) {
    super(String.class, false);
    this.target = target;
    this.replacement = replacement;
  }


  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeString(StringUtils.hasText(value) && StringUtils.hasText(target) ? value
        .replace(target, replacement) : value);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    if (property != null) {
      JsonStringReplace stringReplace = property.getAnnotation(JsonStringReplace.class);
      if (stringReplace == null) {
        throw new RuntimeException("未注解@" + JsonStringReplace.class.getName());
      }
      return new StringReplaceSerializer(stringReplace.value(), stringReplace.replacement());
    }
    return this;
  }

  @Override
  public boolean isEmpty(SerializerProvider prov, String value) {
    return !StringUtils.hasText(value);
  }

  @Override
  public final void serializeWithType(String value, JsonGenerator gen, SerializerProvider provider,
      TypeSerializer typeSer) throws IOException {
    serialize(value, gen, provider);
  }

}