package top.bettercode.simpleframework.web.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 中国（CN）大陆地区身份证号码验证
 * @author P
 *
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IDCardValidator.class)
@Documented
public @interface IDCard {
	// 定义错误消息
	String message() default "错误的身份证号码";

	// 定义所在的组
	Class<?>[] groups() default {};

	// 定义级别条件的严重级别
	Class<? extends Payload>[] payload() default {};
}