package top.bettercode.simpleframework.web.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

/**
 * 中国内地手机号验证
 *
 * @author Peter wu
 */
@Documented
@Constraint(
    validatedBy = {ChinaCellValidator.class}
)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
    ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface ChinaCell {

  String message() default "{chinaCell.notValid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
      ElementType.CONSTRUCTOR, ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @interface List {

    ChinaCell[] value();
  }
}

