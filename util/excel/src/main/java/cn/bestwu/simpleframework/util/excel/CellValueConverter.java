package cn.bestwu.simpleframework.util.excel;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
public interface CellValueConverter {

  /**
   * @param fieldValue 实体属性
   * @param description {@link ExcelField} 注释的字段或方法
   * @param obj 实体对象
   * @return 单元格值
   */
  default String toCell(Object fieldValue, ExcelFieldDescription description, Object obj) {
    return String.valueOf(fieldValue);
  }

  /**
   * @param cellValue 单元格值
   * @param description {@link ExcelField} 注释的字段或方法
   * @param obj 实体对象
   * @return 实体属性
   */
  default Object fromCell(String cellValue, ExcelFieldDescription description, Object obj) {
    return cellValue;
  }


  /**
   * @param description {@link ExcelField} 注释的字段或方法
   * @param obj 实体对象
   * @return 单元格值
   */
  default String null2Cell(ExcelFieldDescription description, Object obj) {
    return "";
  }

  /**
   * @param description {@link ExcelField} 注释的字段或方法
   * @param obj 实体对象
   * @return 实体属性
   */
  default Object emptyfromCell(ExcelFieldDescription description, Object obj) {
    return null;
  }

}
