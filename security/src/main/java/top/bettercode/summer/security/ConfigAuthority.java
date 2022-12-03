package top.bettercode.summer.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口 权限标识
 *
 * @author Peter Wu
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(ConfigAuthority.List.class)
public @interface ConfigAuthority {

  /**
   * @return 需要的权限标识，有任意其中一个即可，不能为null或空白字符串
   */
  String[] value();

  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Target({ElementType.METHOD, ElementType.TYPE})
  @interface List {
    ConfigAuthority[] value();
  }

}
