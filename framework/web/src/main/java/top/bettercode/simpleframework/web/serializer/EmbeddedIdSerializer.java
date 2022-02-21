package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import top.bettercode.simpleframework.support.EmbeddedIdConverter;

@JacksonStdImpl
public class EmbeddedIdSerializer extends StdScalarSerializer<Object> {

  private static final long serialVersionUID = 1759139980737771L;

  public EmbeddedIdSerializer() {
    super(Object.class);
  }

  @Override
  public void serialize(Object value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    gen.writeString(EmbeddedIdConverter.toString(value));

    UnwrappingBeanSerializer beanSerializer = new UnwrappingBeanSerializer(
        (BeanSerializerBase) provider.findValueSerializer(value.getClass()),
        NameTransformer.simpleTransformer("", ""));
    beanSerializer.serialize(value, gen, provider);
  }


}