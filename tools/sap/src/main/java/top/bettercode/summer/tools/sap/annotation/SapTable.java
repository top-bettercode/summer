package top.bettercode.summer.tools.sap.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义SAP输入参数列表（多行数据），映射JcoTable
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SapTable {

  /*
   * SAP方法中对应的table参数名称
   */
  String value();

}
