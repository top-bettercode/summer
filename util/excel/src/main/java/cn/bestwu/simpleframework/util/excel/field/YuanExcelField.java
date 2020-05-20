package cn.bestwu.simpleframework.util.excel.field;

import cn.bestwu.lang.util.MoneyUtil;
import cn.bestwu.simpleframework.util.excel.ExcelField;

/**
 * 元分转换
 */
public class YuanExcelField<T> extends ExcelField<T, Long> {

  @Override
  public String toCell(Long property) {
    return MoneyUtil.toYun(property).toString();
  }

  @Override
  public Long toProperty(String cellValue) {
    return MoneyUtil.toCent(cellValue);
  }
}