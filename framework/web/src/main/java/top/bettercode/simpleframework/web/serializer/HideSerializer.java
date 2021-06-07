package top.bettercode.simpleframework.web.serializer;

import top.bettercode.simpleframework.web.serializer.annotation.JsonHide;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.util.StringUtils;

/**
 * JSON 序列化电话号码自动隐藏
 *
 * @author Peter Wu
 */
@JacksonStdImpl
public class HideSerializer extends StdScalarSerializer<String> implements ContextualSerializer {

  private static final long serialVersionUID = 1L;
  private final int beginKeep;

  private final int endKeep;
  private final boolean alwaysHide;

  public HideSerializer() {
    this(0, 0, true);
  }

  public HideSerializer(int beginKeep, int endKeep, boolean alwaysHide) {
    super(String.class, false);
    this.beginKeep = beginKeep;
    this.endKeep = endKeep;
    this.alwaysHide = alwaysHide;
  }

  public String convert(String value) {
    if (StringUtils.hasText(value)) {
      int keep = beginKeep + endKeep;
      if (value.length() > keep) {
        char[] chars = value.toCharArray();
        int i = 0;
        while (i < chars.length) {
          if (i >= beginKeep && i < (chars.length - endKeep)) {
            chars[i] = '*';
          }
          i++;
        }
        return new String(chars);
      } else if (alwaysHide) {
        char[] chars = new char[value.length()];
        Arrays.fill(chars, '*');
        return new String(chars);
      }
    }
    return value;
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeString(convert(value));
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    if (property != null) {
      JsonHide jsonHide = property.getAnnotation(JsonHide.class);
      if (jsonHide == null) {
        throw new RuntimeException("未注解@" + JsonHide.class.getName());
      }
      return new HideSerializer(jsonHide.beginKeep(), jsonHide.endKeep(), jsonHide.alwaysHide());
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
    gen.writeString(value);
  }

}
