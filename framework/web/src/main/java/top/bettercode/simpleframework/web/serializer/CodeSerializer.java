package top.bettercode.simpleframework.web.serializer;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.support.code.CodeServiceHolder;
import top.bettercode.simpleframework.support.code.ICodeService;
import top.bettercode.simpleframework.web.serializer.annotation.JsonCode;

/**
 * code name Serializer
 *
 * @author Peter Wu
 */
@JacksonStdImpl
public class CodeSerializer extends StdScalarSerializer<Serializable> implements
    ContextualSerializer {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(CodeSerializer.class);

  private final ICodeService codeService;
  private final String codeType;
  private final boolean useExtensionField;

  public CodeSerializer() {
    this("", true);
  }

  public CodeSerializer(String codeType, boolean useExtensionField) {
    this("", codeType, useExtensionField);
  }

  public CodeSerializer(String codeServiceRef, String codeType, boolean useExtensionField) {
    super(Serializable.class, false);
    this.codeType = codeType;
    this.useExtensionField = useExtensionField;
    this.codeService = CodeServiceHolder.get(codeServiceRef);
  }

  @Override
  public void serialize(Serializable value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {

    JsonStreamContext outputContext = gen.getOutputContext();
    String fieldName = outputContext.getCurrentName();
    String codeName;
    String trueCodeType = getCodeType(fieldName);
    if (value instanceof String && ((String) value).contains(",")) {
      String[] split = ((String) value).split(",");
      codeName = StringUtils.arrayToCommaDelimitedString(
          Arrays.stream(split).map(s -> getName(trueCodeType, s.trim())).toArray());
    } else {
      codeName = getName(trueCodeType, value);
    }

    if (useExtensionField) {
      gen.writeObject(value);
      gen.writeStringField(fieldName + "Name", codeName);
    } else {
      gen.writeString(codeName);
    }
  }

  private String getName(String codeType, Serializable code) {
    return this.codeService.getName(codeType, code);
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
      JsonCode annotation = property.getAnnotation(JsonCode.class);
      String codeType = annotation.value();
      codeType = "".equals(codeType) ? property.getName() : codeType;
      return new CodeSerializer(annotation.codeServiceRef(), codeType, annotation.extended());
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