package cn.bestwu.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import java.io.Serializable;

@JacksonStdImpl
public class EmbeddedIdSerializer extends StdScalarSerializer<Serializable> {

  private static final long serialVersionUID = 1759139980737771L;

  public EmbeddedIdSerializer() {
    super(Serializable.class);
  }

  @Override
  public void serialize(Serializable value, JsonGenerator gen,
      SerializerProvider provider) throws IOException {
    gen.writeString(value.toString());
  }


}