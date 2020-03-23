package cn.bestwu.simpleframework.data.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SoftDelete {

  /**
   * @return 默认逻辑未删除值, 默认获取全局配置
   */
  String falseValue() default "";

  /**
   * @return 默认逻辑删除值, 默认获取全局配置
   */
  String trueValue() default "";
}