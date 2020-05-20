package cn.bestwu.simpleframework.util.excel.field;

import cn.bestwu.lang.util.BooleanUtil;
import cn.bestwu.simpleframework.util.excel.ExcelField;

public class BooleanExcelField<T> extends ExcelField<T, Boolean> {

  @Override
  protected String toCell(Boolean property) {
    return property ? "是" : "否";
  }

  @Override
  protected Boolean toProperty(String cellValue) {
    return BooleanUtil.toBoolean(cellValue);
  }

}