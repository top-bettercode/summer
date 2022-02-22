package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import top.bettercode.simpleframework.support.EmbeddedIdConverter;
import top.bettercode.simpleframework.web.serializer.annotation.JsonEmbeddedId;

@JacksonStdImpl
public class EmbeddedIdSerializer extends StdScalarSerializer<Object> implements
    ContextualSerializer {

  private static final long serialVersionUID = 1759139980737771L;
  private final String delimiter;

  public EmbeddedIdSerializer(String delimiter) {
    super(Object.class);
    this.delimiter = delimiter;
  }

  @Override
  public void serialize(Object value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    gen.writeString(EmbeddedIdConverter.toString(value, delimiter));

    UnwrappingBeanSerializer beanSerializer = new UnwrappingBeanSerializer(
        (BeanSerializerBase) provider.findValueSerializer(value.getClass()),
        NameTransformer.simpleTransformer("", ""));
    beanSerializer.serialize(value, gen, provider);
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
    return prov.findNullValueSerializer(property);
  }
}