package top.bettercode.summer.web.serializer.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import top.bettercode.summer.web.serializer.ArraySerializer;

/**
 * @author Peter Wu
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = ArraySerializer.class)
public @interface JsonArray {

  /**
   * @return 字符串分隔符, 字符串以分隔符分隔后序列化为数组
   */
  String value() default ",";

  /**
   * @return 是否使用扩展的字段
   */
  boolean extended() default true;

}
