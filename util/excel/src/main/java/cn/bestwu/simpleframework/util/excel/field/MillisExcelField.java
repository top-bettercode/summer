package cn.bestwu.simpleframework.util.excel.field;

import cn.bestwu.lang.util.LocalDateTimeHelper;
import cn.bestwu.simpleframework.util.excel.ExcelField;
import org.apache.poi.ss.usermodel.DateUtil;

public class MillisExcelField<T> extends ExcelField<T, Long> {

  @Override
  protected String toCell(Long property) {
    return LocalDateTimeHelper.of(property).format(dateTimeFormatter);
  }

  @Override
  protected Object toProperty(String cellValue) {
    return LocalDateTimeHelper.of(DateUtil.getJavaDate(Double.parseDouble(cellValue))).toMillis();
  }
}