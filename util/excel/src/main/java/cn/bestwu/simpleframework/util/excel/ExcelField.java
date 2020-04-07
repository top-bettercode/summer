package cn.bestwu.simpleframework.util.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel注解定义
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelField {

  /**
   * @return 导出字段名（默认调用当前字段的“get”方法，如指定导出字段为对象，请填写“对象名.对象属性”，例：“area.name”、“office.name”）
   */
  String value() default "";

  /**
   * @return 导出字段标题
   */
  String title();

  /**
   * @return 默认值
   */
  String defaultValue() default "";


  /**
   * @return 默认输入值
   */
  String defaultInValue() default "";


  /**
   * @return 导出字段批注
   */
  String comment() default "";

  /**
   * @return 格式
   */
  String pattern() default "";

  /**
   * @return 字段类型
   */
  ExcelFieldType type() default ExcelFieldType.ALL;

  /**
   * @return 导出字段对齐方式
   */
  Alignment align() default Alignment.CENTER;

  /**
   * @return 列宽度，-1表示自动计算
   */
  double width() default -1;

  /**
   * @return 导出字段字段排序（升序）
   */
  int sort() default 0;

  /**
   * @return 单元格值序列化及反序列化转换
   */
  Class<? extends CellValueConverter> converter() default CellValueConverter.class;

  /**
   * @return 单元格转换配置字段
   */
  String converterUsing() default "";
}
