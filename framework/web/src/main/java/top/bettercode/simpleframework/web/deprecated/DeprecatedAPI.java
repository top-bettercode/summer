package top.bettercode.simpleframework.web.deprecated;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * 已弃用的接口
 *
 * @author Peter Wu
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DeprecatedAPI {

  /**
   * @return 提示消息,默认“该功能已弃用”
   */
  @AliasFor("message")
  String value() default "deprecated.api";

  /**
   * @return 提示消息,默认“该功能已弃用”
   */
  @AliasFor("value")
  String message() default "deprecated.api";
}
