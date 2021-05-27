package cn.bestwu.simpleframework.security.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.HttpMethod;

/**
 * 注解了此类的方法，需要Client Authorize
 *
 * @author Peter Wu
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ClientAuthorize {

	/**
	 * @return 接口的URI，默认由接口指定
	 */
	String[] value() default {};

	/**
	 * @return 请求方法
	 */
	HttpMethod method() default HttpMethod.POST;
}
