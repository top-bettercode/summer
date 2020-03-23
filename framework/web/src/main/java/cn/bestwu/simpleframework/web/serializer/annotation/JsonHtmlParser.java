package cn.bestwu.simpleframework.web.serializer.annotation;

import cn.bestwu.simpleframework.web.serializer.HtmlParserSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = HtmlParserSerializer.class)
public @interface JsonHtmlParser {

  /**
   * @return length
   */
  int value() default -1;
}