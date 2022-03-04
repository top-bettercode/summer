package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import org.springframework.util.StringUtils;
import top.bettercode.lang.util.HtmlUtil;
import top.bettercode.simpleframework.web.serializer.annotation.JsonHtmlParser;

/**
 * @author Peter Wu
 */
@JacksonStdImpl
public class HtmlParserSerializer extends StdScalarSerializer<String> implements
    ContextualSerializer {

  private static final long serialVersionUID = 1L;

  private final int length;

  public HtmlParserSerializer() {
    super(String.class, false);
    length = -1;
  }

  public HtmlParserSerializer(int length) {
    super(String.class, false);
    this.length = length;
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    if (StringUtils.hasText(value)) {
      if (length == -1) {
        value = HtmlUtil.parseHtml(value);
      } else {
        value = HtmlUtil.subParseHtml(value, length);
      }
    }
    gen.writeString(value);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
      throws JsonMappingException {
    if (property != null && String.class.isAssignableFrom(property.getType().getRawClass())) {
      JsonHtmlParser htmlParser = property.getAnnotation(JsonHtmlParser.class);
      int length = htmlParser == null ? -1 : htmlParser.value();
      return new HtmlParserSerializer(length);
    }
    return prov.findNullValueSerializer(property);
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