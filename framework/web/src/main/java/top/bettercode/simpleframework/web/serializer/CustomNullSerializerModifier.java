package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.util.List;
import top.bettercode.simpleframework.config.JacksonExtProperties;
import top.bettercode.simpleframework.web.serializer.annotation.JsonDefault;

public class CustomNullSerializerModifier extends BeanSerializerModifier {

  private final JacksonExtProperties jacksonExtProperties;

  public CustomNullSerializerModifier(
      JacksonExtProperties jacksonExtProperties) {
    this.jacksonExtProperties = jacksonExtProperties;
  }


  @Override
  public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
      BeanDescription beanDesc,
      List<BeanPropertyWriter> beanProperties) {
    for (BeanPropertyWriter writer : beanProperties) {
      if (!writer.hasNullSerializer()) {
        JsonDefault annotation = writer.getAnnotation(JsonDefault.class);
        String defaultValue = null;
        String extendedValue = null;
        if (annotation != null) {
          defaultValue = annotation.value();
          extendedValue = annotation.extended();
        }
        if (defaultValue != null
            || config.getDefaultPropertyInclusion().getValueInclusion() != Include.NON_NULL) {
          writer.assignNullSerializer(new CustomNullSerializer(writer, defaultValue, extendedValue,
              jacksonExtProperties));
        }
      }
    }
    return beanProperties;
  }

}