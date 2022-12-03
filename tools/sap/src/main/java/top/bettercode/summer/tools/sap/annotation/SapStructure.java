package top.bettercode.summer.tools.sap.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义SAP输入参数为结构化数据对象, 对应SAP中的 JCoStructure
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SapStructure {

  /*
   * SAP方法中对应的结构化对象参数名称
   */
  String value();

}
