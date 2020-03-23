package cn.bestwu.simpleframework.util.excel.converter;

import cn.bestwu.lang.util.MoneyUtil;
import cn.bestwu.simpleframework.util.excel.CellValueConverter;
import cn.bestwu.simpleframework.util.excel.ExcelFieldDescription;

/**
 * 元分转换
 */
public class YuanConverter implements CellValueConverter {

  private static final YuanConverter instance = new YuanConverter();

  private YuanConverter() {
  }

  public static YuanConverter newInstance() {
    return instance;
  }

  @Override
  public String toCell(Object fieldValue, ExcelFieldDescription description, Object obj) {
    Long cent = (Long) fieldValue;
    return MoneyUtil.toYun(cent).toString();
  }

  @Override
  public Object fromCell(String cellValue, ExcelFieldDescription description, Object obj) {
    return MoneyUtil.toCent(cellValue);
  }
}