package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.web.serializer.annotation.JsonDefault;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.util.List;

public class CustomNullSerializerModifier extends BeanSerializerModifier {

  private boolean defaultEmpty;

  public CustomNullSerializerModifier(boolean defaultEmpty) {
    this.defaultEmpty = defaultEmpty;
  }

  @Override
  public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
      BeanDescription beanDesc,
      List<BeanPropertyWriter> beanProperties) {
    for (BeanPropertyWriter writer : beanProperties) {
      if (!writer.hasNullSerializer()) {
        JsonDefault annotation = writer.getAnnotation(JsonDefault.class);
        String defaultValue = null;
        if (annotation != null) {
          defaultValue = annotation.value();
        }
        if (defaultValue != null
            || config.getDefaultPropertyInclusion().getValueInclusion() != Include.NON_NULL) {
          writer.assignNullSerializer(new CustomNullSerializer(writer, defaultValue, defaultEmpty));
        }
      }
    }
    return beanProperties;
  }

}