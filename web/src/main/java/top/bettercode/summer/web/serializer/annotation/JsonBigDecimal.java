package top.bettercode.summer.web.serializer.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;
import top.bettercode.summer.web.serializer.BigDecimalSerializer;

/**
 * @author Peter Wu
 * @since 0.1.15
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = BigDecimalSerializer.class)
public @interface JsonBigDecimal {

  /**
   * @return 小数位数
   */
  int scale() default 2;

  RoundingMode roundingMode() default RoundingMode.HALF_UP;

  /**
   * @return 序列化为字符
   */
  boolean toPlainString() default false;

  /**
   * @return 当小数位为零时，是否精简小数位
   */
  boolean reduceFraction() default false;

  /**
   * @return 扩展序列化百分比字段
   */
  boolean percent() default false;
}
