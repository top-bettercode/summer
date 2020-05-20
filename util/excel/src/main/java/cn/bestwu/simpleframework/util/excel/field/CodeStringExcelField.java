package cn.bestwu.simpleframework.util.excel.field;

import cn.bestwu.simpleframework.util.excel.ExcelField;
import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import java.util.Arrays;
import org.springframework.util.StringUtils;

public class CodeStringExcelField<T> extends ExcelField<T, String> {

  private String codeType;

  //--------------------------------------------

  public CodeStringExcelField<T> setCodeType(String codeType) {
    this.codeType = codeType;
    return this;
  }

  private String getCodeType() {
    return codeType == null ? propertyName : codeType;
  }

  //--------------------------------------------

  @Override
  protected String toCell(String property) {
    String codeType = getCodeType();
    if (property.contains(",")) {
      String[] split = property.split(",");
      return StringUtils.arrayToCommaDelimitedString(
          Arrays.stream(split).map(s -> CodeSerializer.getName(codeType, s.trim())).toArray());
    } else {
      return CodeSerializer.getName(codeType, property);
    }
  }

  @Override
  protected Object toProperty(String cellValue) {
    String codeType = getCodeType();
    if (cellValue.contains(",")) {
      String[] split = cellValue.split(",");
      return StringUtils.arrayToCommaDelimitedString(
          Arrays.stream(split).map(s -> CodeSerializer.getCode(codeType, s.trim())).toArray());
    } else {
      return CodeSerializer.getCode(codeType, cellValue);
    }
  }


}