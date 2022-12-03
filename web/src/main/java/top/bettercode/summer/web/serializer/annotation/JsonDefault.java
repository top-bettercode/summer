package top.bettercode.summer.web.serializer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JsonDefault {

  /**
   * @return 默认值
   */
  String value() default "";

  /**
   * @return 使用另一个字段的值
   */
  String fieldName() default "";

  /**
   * @return 扩展默认值
   */
  String extended() default "";
}