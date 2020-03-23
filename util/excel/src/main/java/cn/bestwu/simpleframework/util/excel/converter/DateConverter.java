package cn.bestwu.simpleframework.util.excel.converter;

import cn.bestwu.lang.util.LocalDateTimeHelper;
import cn.bestwu.simpleframework.util.excel.CellValueConverter;
import cn.bestwu.simpleframework.util.excel.ExcelFieldDescription;
import java.util.Date;
import org.apache.poi.ss.usermodel.DateUtil;

public class DateConverter implements CellValueConverter {

  private static final DateConverter instance = new DateConverter();

  private DateConverter() {
  }

  public static DateConverter newInstance() {
    return instance;
  }

  @Override
  public String toCell(Object fieldValue, ExcelFieldDescription description, Object obj) {
    return LocalDateTimeHelper.of((Date) fieldValue).format(description.getDateTimeFormatter());
  }

  @Override
  public Object fromCell(String cellValue, ExcelFieldDescription description, Object obj) {
    return LocalDateTimeHelper.of(DateUtil.getJavaDate(Double.parseDouble(cellValue))).toDate();
  }
}