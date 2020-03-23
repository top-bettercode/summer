package cn.bestwu.simpleframework.util.excel.converter;

import cn.bestwu.lang.util.BooleanUtil;
import cn.bestwu.simpleframework.util.excel.CellValueConverter;
import cn.bestwu.simpleframework.util.excel.ExcelFieldDescription;

public class BooleanFieldConverter implements CellValueConverter {

  private static final BooleanFieldConverter instance = new BooleanFieldConverter();

  private BooleanFieldConverter() {
  }

  public static BooleanFieldConverter newInstance() {
    return instance;
  }

  @Override
  public String toCell(Object fieldValue, ExcelFieldDescription description, Object obj) {
    return BooleanUtil.toBoolean(String.valueOf(fieldValue)) ? "是" : "否";
  }

  @Override
  public Object fromCell(String cellValue, ExcelFieldDescription description, Object obj) {
    return BooleanUtil.toBoolean(cellValue);
  }
}