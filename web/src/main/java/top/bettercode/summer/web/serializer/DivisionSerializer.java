package top.bettercode.summer.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.summer.web.serializer.annotation.JsonDivision;
import top.bettercode.summer.web.support.gb2260.Division;
import top.bettercode.summer.web.support.gb2260.GB2260;
import top.bettercode.summer.web.support.gb2260.InvalidCodeException;

/**
 * @author Peter Wu
 */
@JacksonStdImpl
public class DivisionSerializer extends StdScalarSerializer<String> implements
    ContextualSerializer {

  private final Logger log = LoggerFactory.getLogger(DivisionSerializer.class);
  private static final long serialVersionUID = 1L;
  private final boolean vnode;

  public DivisionSerializer() {
    this(false);
  }


  public DivisionSerializer(boolean vnode) {
    super(String.class, false);
    this.vnode = vnode;
  }


  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeString(value);

    JsonStreamContext outputContext = gen.getOutputContext();
    String fieldName = outputContext.getCurrentName();
    gen.writeFieldName(fieldName + "Path");
    try {
      Division division = GB2260.getDivision(value);
      gen.writeObject(division.codes(vnode));
    } catch (InvalidCodeException e) {
      log.warn(e.getMessage());
      gen.writeNull();
    }
  }


  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
      throws JsonMappingException {
    if (property != null) {
      JsonDivision annotation = property.getAnnotation(JsonDivision.class);
      return new DivisionSerializer(annotation.value());
    }
    return prov.findNullValueSerializer(null);
  }


  @Override
  public boolean isEmpty(SerializerProvider prov, String value) {
    return value.length() == 0;
  }

  @Override
  public final void serializeWithType(String value, JsonGenerator gen, SerializerProvider
      provider,
      TypeSerializer typeSer) throws IOException {
    serialize(value, gen, provider);
  }

}
