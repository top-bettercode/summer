package top.bettercode.summer.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.lang.util.DirectFieldAccessFallbackBeanWrapper;
import top.bettercode.summer.web.config.JacksonExtProperties;
import top.bettercode.summer.web.serializer.annotation.JsonBigDecimal;
import top.bettercode.summer.web.serializer.annotation.JsonCode;
import top.bettercode.summer.web.serializer.annotation.JsonUrl;
import top.bettercode.summer.web.support.ApplicationContextHolder;

/**
 * @author Peter Wu
 */
public class CustomNullSerializer extends StdSerializer<Object> {

  private static final long serialVersionUID = 1L;
  private final Class<?> type;
  private final String defaultValue;
  private final String extendedValue;
  private final String fieldName;
  private final JacksonExtProperties jacksonExtProperties;
  private final boolean isArray;
  private final BeanPropertyWriter writer;

  public CustomNullSerializer(BeanPropertyWriter writer, String defaultValue,
      String fieldName, String extendedValue, JacksonExtProperties jacksonExtProperties) {
    super(Object.class);
    this.writer = writer;
    this.type = writer.getType().getRawClass();
    this.defaultValue = defaultValue;
    this.fieldName = fieldName;
    this.extendedValue = extendedValue;
    this.jacksonExtProperties = jacksonExtProperties;
    isArray = type.isArray() || (Collection.class.isAssignableFrom(type) && !Map.class
        .isAssignableFrom(type));
  }

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    JsonStreamContext outputContext = gen.getOutputContext();
    String fieldName = outputContext.getCurrentName();
    Object defaultValue = this.defaultValue;
    if ("".equals(this.defaultValue) && StringUtils.hasText(this.fieldName)) {
      Object o = gen.currentValue();
      DirectFieldAccessFallbackBeanWrapper beanWrapper = new DirectFieldAccessFallbackBeanWrapper(
          o);
      defaultValue = beanWrapper.getPropertyValue(this.fieldName);
    }

    if (defaultValue == null) {
      if (jacksonExtProperties.getDefaultEmpty()) {
        serializeJsonCode(value, gen, provider, fieldName);
      } else {
        gen.writeNull();
        serializeExtend(gen, fieldName, false);
      }
    } else {
      if (defaultValue instanceof String && !StringUtils.hasText((String) defaultValue)) {
        if (serializeJsonCode(value, gen, provider, fieldName)) {
          return;
        }
        return;
      }
      JsonBigDecimal jsonBigDecimal = writer.getAnnotation(JsonBigDecimal.class);
      Object val = ApplicationContextHolder.getConversionService().convert(defaultValue, type);
      if (jsonBigDecimal != null) {
        new BigDecimalSerializer(jsonBigDecimal.scale(), jsonBigDecimal.roundingMode(),
            jsonBigDecimal.toPlainString(),
            jsonBigDecimal.reduceFraction(), jsonBigDecimal.percent()).serialize(
            (BigDecimal) val, gen,
            provider);
        return;
      }
      JsonCode jsonCode = writer.getAnnotation(JsonCode.class);
      if (jsonCode != null) {
        new CodeSerializer(jsonCode.value(), jsonCode.extended())
            .serialize((Serializable) val, gen, provider);
        return;
      }
      JsonUrl jsonUrl = writer.getAnnotation(JsonUrl.class);
      if (jsonUrl != null && val != null) {
        new UrlSerializer(jsonUrl.value(), jsonUrl.urlFieldName(), jsonUrl.extended(),
            jsonUrl.asMap(),
            jsonUrl.separator(), jsonUrl.defaultValue(), jsonUrl.mapper()).serialize(val, gen,
            provider);
        return;
      }
      gen.writeObject(val);
    }
  }

  private boolean serializeJsonCode(Object value, JsonGenerator gen, SerializerProvider provider,
      String fieldName) throws IOException {
    JsonCode jsonCode = writer.getAnnotation(JsonCode.class);
    if (jsonCode != null) {
      new CodeSerializer(jsonCode.value(), jsonCode.extended())
          .serialize("", gen, provider);
      return true;
    }
    serializeNull(gen, type, value);
    serializeExtend(gen, fieldName, true);
    return false;
  }

  private void serializeExtend(JsonGenerator gen, String fieldName,
      boolean defaultEmpty)
      throws IOException {
    String value = StringUtils.hasText(extendedValue) ? extendedValue : (defaultEmpty ? "" : null);
    JsonCode jsonCode = writer.getAnnotation(JsonCode.class);
    if (jsonCode != null && jsonCode.extended()) {
      gen.writeStringField(fieldName + "Name", value);
      return;
    }
    JsonBigDecimal jsonBigDecimal = writer.getAnnotation(JsonBigDecimal.class);
    if (jsonBigDecimal != null && jsonBigDecimal.percent()) {
      gen.writeStringField(fieldName + "Pct", value);
      return;
    }
    JsonUrl jsonUrl = writer.getAnnotation(JsonUrl.class);
    if (jsonUrl != null && jsonUrl.extended()) {
      String urlFieldName = jsonUrl.urlFieldName();
      if ("".equals(urlFieldName)) {
        if (isArray) {
          urlFieldName = fieldName + "Urls";
        } else {
          urlFieldName = fieldName + "Url";
        }
      }
      if (isArray) {
        gen.writeObjectField(urlFieldName, defaultEmpty ? Collections.EMPTY_LIST : null);
      } else {
        gen.writeStringField(urlFieldName, value);
      }
    }
  }

  public void serializeNull(JsonGenerator gen, Class<?> type, Object value)
      throws IOException {
    if (type == String.class) {
      gen.writeString("");
    } else if (isArray) {
      gen.writeObject(Collections.EMPTY_LIST);
    } else if (type.getClassLoader() != null || Map.class.isAssignableFrom(type)) {
      gen.writeObject(Collections.emptyMap());
    } else {
      gen.writeObject(value);
    }
  }

  public static boolean support(Class<?> type) {
    return (type == String.class) || (type.isArray() || (Collection.class.isAssignableFrom(type)
        && !Map.class
        .isAssignableFrom(type))) || (type.getClassLoader() != null || Map.class
        .isAssignableFrom(type));
  }

  @Override
  public final void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
      TypeSerializer typeSer) throws IOException {
    serialize(value, gen, provider);
  }
}
