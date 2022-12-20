package top.bettercode.summer.web.form;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Peter Wu
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FormDuplicateCheck {

  /**
   * 使用 {@link RedisFormkeyService} 时，支持指定过期时间，单位秒
   *
   * @return form key有效时间
   */
  long expireSeconds() default -1;

  /**
   * @return 提示信息
   */
  String message() default FormDuplicateCheckInterceptor.DEFAULT_MESSAGE;

}
