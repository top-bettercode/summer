package top.bettercode.summer.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import top.bettercode.summer.web.serializer.annotation.JsonEmbeddedId;
import top.bettercode.summer.web.support.EmbeddedIdConverter;

@JacksonStdImpl
public class EmbeddedIdSerializer extends StdScalarSerializer<Object> implements
    ContextualSerializer {

  private static final long serialVersionUID = 1L;
  private final String delimiter;


  public EmbeddedIdSerializer() {
    this(EmbeddedIdConverter.DELIMITER);
  }

  public EmbeddedIdSerializer(String delimiter) {
    super(Object.class);
    this.delimiter = delimiter;
  }

  @Override
  public void serialize(Object value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    gen.writeString(EmbeddedIdConverter.toString(value, delimiter));
  }


  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
      throws JsonMappingException {
    if (property != null) {
      JsonEmbeddedId annotation = property.getAnnotation(JsonEmbeddedId.class);
      String delimiter = annotation.value();
      delimiter = "".equals(delimiter) ? EmbeddedIdConverter.DELIMITER : delimiter;
      return new EmbeddedIdSerializer(delimiter);
    }
    return prov.findNullValueSerializer(null);
  }
}