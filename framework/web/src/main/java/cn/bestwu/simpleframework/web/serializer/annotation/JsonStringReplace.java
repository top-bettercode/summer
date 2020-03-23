package cn.bestwu.simpleframework.web.serializer.annotation;

import cn.bestwu.simpleframework.web.serializer.StringReplaceSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = StringReplaceSerializer.class)
public @interface JsonStringReplace {

  /**
   * @return target
   */
  String value() default "";

  String replacement() default "";
}