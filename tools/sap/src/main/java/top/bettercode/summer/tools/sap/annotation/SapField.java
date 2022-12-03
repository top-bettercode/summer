package top.bettercode.summer.tools.sap.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * JavaBean对应SAP接口服务中的参数名称
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SapField {

  /*
   * SAP方法中对应的参数名称
   */
  String value();

}
