package cn.bestwu.simpleframework.util.excel.field;

import cn.bestwu.simpleframework.util.excel.ExcelField;
import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import java.util.Arrays;
import org.springframework.util.StringUtils;

public class CodeExcelField<T> extends ExcelField<T, Integer> {

  private String codeType;

  //--------------------------------------------

  public CodeExcelField<T> setCodeType(String codeType) {
    this.codeType = codeType;
    return this;
  }

  private String getCodeType() {
    return codeType == null ? propertyName : codeType;
  }

  //--------------------------------------------

  @Override
  protected String toCell(Integer property) {
    String codeType = getCodeType();
    return CodeSerializer.getName(codeType, property);
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