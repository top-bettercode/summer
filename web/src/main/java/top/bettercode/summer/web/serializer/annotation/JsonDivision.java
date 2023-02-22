package top.bettercode.summer.web.serializer.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import top.bettercode.summer.web.serializer.DivisionSerializer;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = DivisionSerializer.class)
public @interface JsonDivision {

  /**
   * @return 是否使用虚拟节点
   */
  boolean value() default false;

}
