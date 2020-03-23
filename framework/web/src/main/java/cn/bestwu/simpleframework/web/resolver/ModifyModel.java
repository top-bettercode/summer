package cn.bestwu.simpleframework.web.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 修改资源
 *
 * @author Peter Wu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ModifyModel {

  String OLD_MODEL = "OLD_MODEL";

  /**
   * @return 修改的资源的原始模型类型
   */
  Class<?> value() default Object.class;

  String idParameter() default "id";
}
