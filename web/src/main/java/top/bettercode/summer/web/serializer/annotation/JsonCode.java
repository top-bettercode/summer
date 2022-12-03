package top.bettercode.summer.web.serializer.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import top.bettercode.summer.web.serializer.CodeSerializer;
import top.bettercode.summer.web.support.code.CodeServiceHolder;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = CodeSerializer.class)
public @interface JsonCode {

  /**
   * @return codeType
   */
  String value() default "";

  /**
   * @return 是否使用扩展的字段序列化
   */
  boolean extended() default true;

  /**
   * @return codeService beanName default: {@link CodeServiceHolder#getDefault()}
   */
  String codeServiceRef() default "";
}