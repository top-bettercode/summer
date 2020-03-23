package cn.bestwu.simpleframework.util.excel.converter;

import cn.bestwu.lang.util.LocalDateTimeHelper;
import cn.bestwu.simpleframework.util.excel.CellValueConverter;
import cn.bestwu.simpleframework.util.excel.ExcelFieldDescription;
import org.apache.poi.ss.usermodel.DateUtil;

public class MillisConverter implements CellValueConverter {

  private static final MillisConverter instance = new MillisConverter();

  private MillisConverter() {
  }

  public static MillisConverter newInstance() {
    return instance;
  }

  @Override
  public String toCell(Object fieldValue, ExcelFieldDescription description, Object obj) {
    return LocalDateTimeHelper.of((Long) fieldValue).format(description.getDateTimeFormatter());
  }

  @Override
  public Object fromCell(String cellValue, ExcelFieldDescription description, Object obj) {
    return LocalDateTimeHelper.of(DateUtil.getJavaDate(Double.parseDouble(cellValue))).toMillis();
  }
}