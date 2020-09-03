package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.support.code.ICodeService;
import cn.bestwu.simpleframework.web.serializer.annotation.JsonCode;
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
import java.io.Serializable;
import java.util.Arrays;
import org.springframework.util.StringUtils;

/**
 * code name Serializer
 *
 * @author Peter Wu
 */
@JacksonStdImpl
public class CodeSerializer extends StdScalarSerializer<Serializable> implements
    ContextualSerializer {

  private static final long serialVersionUID = 1L;

  private static ICodeService codeService;
  private final String codeType;

  public CodeSerializer() {
    this("");
  }

  public CodeSerializer(String codeType) {
    super(Serializable.class, false);
    this.codeType = codeType;
  }

  public static void setCodeService(ICodeService codeService) {
    CodeSerializer.codeService = codeService;
  }

  public static String getName(String codeType, Serializable code) {
    return codeService.getName(codeType, code);
  }

  public static Serializable getCode(String codeType, String name) {
    return codeService.getCode(codeType, name);
  }

  @Override
  public void serialize(Serializable value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeObject(value);

    JsonStreamContext outputContext = gen.getOutputContext();
    String fieldName = outputContext.getCurrentName();
    String codeName = value.toString().trim();
    if (StringUtils.hasText(value.toString())) {
      String trueCodeType = getCodeType(fieldName);
      if (value instanceof String && ((String) value).contains(",")) {
        String[] split = ((String) value).split(",");
        codeName = StringUtils.arrayToCommaDelimitedString(
            Arrays.stream(split).map(s -> getName(trueCodeType, s.trim())).toArray());
      } else {
        codeName = getName(trueCodeType, value);
      }
    }
    gen.writeStringField(fieldName + "Name", codeName);
  }

  private String getCodeType(String fieldName) {
    if ("".equals(codeType)) {
      return fieldName;
    }
    return codeType;
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
      throws JsonMappingException {
    if (property != null) {
      JsonCode dicCodeAnno = property.getAnnotation(JsonCode.class);
      String codeType = dicCodeAnno == null ? property.getName() : dicCodeAnno.value();
      return new CodeSerializer(codeType);
    }
    return prov.findNullValueSerializer(property);
  }


  @Override
  public final void serializeWithType(Serializable value, JsonGenerator gen,
      SerializerProvider provider,
      TypeSerializer typeSer) throws IOException {
    serialize(value, gen, provider);
  }

}