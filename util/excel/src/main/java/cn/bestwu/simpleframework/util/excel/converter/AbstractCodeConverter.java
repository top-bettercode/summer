package cn.bestwu.simpleframework.util.excel.converter;

import cn.bestwu.simpleframework.util.excel.CellValueConverter;
import cn.bestwu.simpleframework.util.excel.ExcelField;
import cn.bestwu.simpleframework.util.excel.ExcelFieldDescription;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.util.StringUtils;

public abstract class AbstractCodeConverter implements CellValueConverter {

  private boolean intCode;

  public AbstractCodeConverter(boolean intCode) {
    this.intCode = intCode;
  }

  protected String getCodeType(ExcelFieldDescription description) {
    AccessibleObject accessibleObject = description.getAccessibleObject();
    String codeType = description.getExcelField().converterUsing();
    if (!StringUtils.hasText(codeType)) {
      if (accessibleObject instanceof Field) {
        codeType = ((Field) accessibleObject).getName();
      } else if (accessibleObject instanceof Method) {
        codeType = ((Method) accessibleObject).getName();
        if (codeType.startsWith("get")) {
          codeType = StringUtils.uncapitalize(codeType.substring(3));
        }
      } else {
        try {
          throw new RuntimeException(
              "请配置@" + ExcelField.class.getName() + "的" + ExcelField.class
                  .getMethod("converterUsing").getName()
                  + "属性");
        } catch (NoSuchMethodException ignored) {
        }
      }
    }
    return codeType;
  }

  @Override
  public String toCell(Object fieldValue, ExcelFieldDescription description, Object obj) {
    Serializable code = (Serializable) fieldValue;
    String codeType = getCodeType(description);
    if (code instanceof String && ((String) code).contains(",")) {
      String[] split = ((String) code).split(",");
      return StringUtils.arrayToCommaDelimitedString(
          Arrays.stream(split).map(s -> getName(codeType, s.trim())).toArray());
    } else {
      return getName(codeType, code);
    }
  }

  public abstract String getName(String codeType, Serializable code);

  @Override
  public Object fromCell(String cellValue, ExcelFieldDescription description, Object obj) {
    String codeType = getCodeType(description);
    if (cellValue.contains(",")) {
      String[] split = cellValue.split(",");
      return StringUtils.arrayToCommaDelimitedString(
          Arrays.stream(split).map(s -> getCode(codeType, s.trim())).toArray());
    } else {
      return getCode(codeType, cellValue);
    }
  }

  protected abstract Object getCode(String codeType, String name);

}