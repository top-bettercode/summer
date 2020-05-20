package cn.bestwu.simpleframework.util.excel;

import cn.bestwu.lang.util.LocalDateTimeHelper;
import cn.bestwu.simpleframework.util.excel.field.BooleanExcelField;
import cn.bestwu.simpleframework.util.excel.field.CodeExcelField;
import cn.bestwu.simpleframework.util.excel.field.CodeStringExcelField;
import cn.bestwu.simpleframework.util.excel.field.MillisExcelField;
import cn.bestwu.simpleframework.util.excel.field.YuanExcelField;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Excel 字段定义
 */
public class ExcelField<T, P> {

  /**
   * 导出字段标题
   */
  private String title;

  /**
   * 导出字段批注
   */
  private String comment = "";

  /**
   * 默认值
   */
  private P defaultValue;

  /**
   * 格式
   */
  private String pattern = "";

  /**
   * 导出字段对齐方式
   */
  private Alignment align = Alignment.CENTER;

  /**
   * 列宽度，-1表示自动计算
   */
  private double width = -1;

  /**
   * 实体转单元格值
   */
  private ExcelConverter<T, P> cellExport;

  /**
   * 单元格值设置实体属性
   */
  private ExcelCellSetter<T, P> propertySetter;

  protected Class<T> entityType;
  protected Class<?> propertyType;
  protected String propertyName;
  protected DateTimeFormatter dateTimeFormatter;

  //--------------------------------------------
  public static <T, P> ExcelField<T, P> export(ExcelConverter<T, P> cellExport) {
    return new ExcelField<T, P>().of(cellExport);
  }

  public static <T, P> ExcelField<T, P> setter(ExcelCellSetter<T, P> propertySetter) {
    return new ExcelField<T, P>().propertySetter(propertySetter);
  }

  //--------------------------------------------
  public static <T> YuanExcelField<T> exportYuan(ExcelConverter<T, Long> cellExport) {
    YuanExcelField<T> excelField = new YuanExcelField<>();
    excelField.of(cellExport);
    return excelField;
  }

  public static <T> YuanExcelField<T> setterYuan(ExcelCellSetter<T, Long> propertySetter) {
    YuanExcelField<T> excelField = new YuanExcelField<>();
    excelField.propertySetter(propertySetter);
    return excelField;
  }

  //--------------------------------------------
  public static <T> MillisExcelField<T> exportMillis(ExcelConverter<T, Long> cellExport) {
    MillisExcelField<T> excelField = new MillisExcelField<>();
    excelField.of(cellExport);
    return excelField;
  }

  public static <T> MillisExcelField<T> setterMillis(ExcelCellSetter<T, Long> propertySetter) {
    MillisExcelField<T> excelField = new MillisExcelField<>();
    excelField.propertySetter(propertySetter);
    return excelField;
  }

  //--------------------------------------------
  public static <T> BooleanExcelField<T> exportBoolean(ExcelConverter<T, Boolean> cellExport) {
    BooleanExcelField<T> excelField = new BooleanExcelField<>();
    excelField.of(cellExport);
    return excelField;
  }

  public static <T> BooleanExcelField<T> setterBoolean(ExcelCellSetter<T, Boolean> propertySetter) {
    BooleanExcelField<T> excelField = new BooleanExcelField<>();
    excelField.propertySetter(propertySetter);
    return excelField;
  }

  //--------------------------------------------
  public static <T> CodeExcelField<T> exportCode(ExcelConverter<T, Integer> cellExport) {
    CodeExcelField<T> excelField = new CodeExcelField<>();
    excelField.of(cellExport);
    return excelField;
  }

  public static <T> CodeExcelField<T> setterCode(ExcelCellSetter<T, Integer> propertySetter) {
    CodeExcelField<T> excelField = new CodeExcelField<>();
    excelField.propertySetter(propertySetter);
    return excelField;
  }

  //--------------------------------------------
  public static <T> CodeStringExcelField<T> exportCodeString(ExcelConverter<T, String> cellExport) {
    CodeStringExcelField<T> excelField = new CodeStringExcelField<>();
    excelField.of(cellExport);
    return excelField;
  }

  public static <T> CodeStringExcelField<T> setterCodeString(
      ExcelCellSetter<T, String> propertySetter) {
    CodeStringExcelField<T> excelField = new CodeStringExcelField<>();
    excelField.propertySetter(propertySetter);
    return excelField;
  }
  //--------------------------------------------

  /**
   * @param obj 实体对象
   * @return 单元格值
   */
  public Object cellValue(T obj) {
    P property = cellExport.convert(obj);
    if (property == null) {
      property = defaultValue;
    }
    return toCell(property);
  }


  /**
   * @param obj            实体对象
   * @param cellValue      单元格值
   * @param validator      参数验证
   * @param validateGroups 参数验证组
   */
  @SuppressWarnings("unchecked")
  public void setProperty(T obj, String cellValue, Validator validator,
      Class<?>[] validateGroups) {
    try {
      P property;
      if (!StringUtils.hasText(cellValue)) {
        property = defaultValue;
      } else {
        property = (P) toProperty(cellValue);
      }
      propertySetter.set(obj, property);
      Set<ConstraintViolation<Object>> constraintViolations = validator
          .validateProperty(obj, propertyName, validateGroups);
      if (!constraintViolations.isEmpty()) {
        throw new ConstraintViolationException(constraintViolations);
      }
    } catch (Exception e) {
      String message = e.getMessage();
      throw new IllegalArgumentException(StringUtils.hasText(message) ? message : "typeMismatch",
          e);
    }
  }

//--------------------------------------------

  /**
   * 属性转单元格值
   *
   * @param property 属性值
   * @return 单元格值
   */
  protected Object toCell(P property) {
    if (property == null) {
      return "";
    } else if (propertyType.equals(BigDecimal.class)) {
      return ((BigDecimal) property).toPlainString();
    } else {
      return property;
    }
  }

  /**
   * 单元格值转属性值
   *
   * @param cellValue 单元格值
   * @return 属性值
   */
  protected Object toProperty(String cellValue) {
    if (propertyType == String.class) {
      return cellValue;
    } else if (propertyType == Integer.class) {
      return Double.valueOf(cellValue).intValue();
    } else if (propertyType == Long.class) {
      return Double.valueOf(cellValue).longValue();
    } else if (propertyType == BigDecimal.class) {
      return new BigDecimal(cellValue);
    } else if (propertyType == Double.class) {
      return Double.valueOf(cellValue);
    } else if (propertyType == Float.class) {
      return Float.valueOf(cellValue);
    } else if (propertyType == Date.class) {
      Date date;
      try {
        date = DateUtil.getJavaDate(Double.parseDouble(cellValue));
      } catch (NumberFormatException e) {
        date = LocalDateTimeHelper.parse(cellValue, dateTimeFormatter).toDate();
      }
      return date;
    } else {
      throw new IllegalArgumentException("类型不正确,期待的数据类型:" + propertyType.getName());
    }
  }

  //--------------------------------------------

  /**
   * @return 单元格格式
   */
  public String getCellFormat() {
    String cellFormatString = pattern;
    if (!StringUtils.hasText(cellFormatString)) {
      cellFormatString = getCellFormat(propertyType);
    }
    return cellFormatString;
  }

  private String getCellFormat(Class<?> fieldType) {
    if (fieldType.equals(Integer.class)) {
      return "0";
    } else if (fieldType.equals(Long.class)) {
      return "0";
    } else if (fieldType.equals(BigDecimal.class)) {
      return "0.00";
    } else if (fieldType.equals(Double.class)) {
      return "0.00";
    } else if (fieldType.equals(Float.class)) {
      return "0.00";
    } else if (fieldType.equals(Date.class)) {
      return "yyyy-MM-dd HH:mm";
    }
    return "@";
  }

  private String resolvePropertyName(String getMethodName) {
    if (getMethodName.startsWith("get") || getMethodName.startsWith("set")) {
      getMethodName = getMethodName.substring(3);
    } else if (getMethodName.startsWith("is")) {
      getMethodName = getMethodName.substring(2);
    }
    return StringUtils.uncapitalize(getMethodName);
  }
  //--------------------------------------------

  public String title() {
    return title;
  }

  public String comment() {
    return comment;
  }

  public Alignment align() {
    return align;
  }

  public double width() {
    return width;
  }

  //--------------------------------------------
  @SuppressWarnings("unchecked")
  public ExcelField<T, P> of(ExcelConverter<T, P> cellExport) {
    this.cellExport = cellExport;
    try {
      Method writeReplace = cellExport.getClass().getDeclaredMethod("writeReplace");
      writeReplace.setAccessible(true);
      SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(cellExport);
      String implMethodName = serializedLambda.getImplMethodName();
      propertyName = resolvePropertyName(implMethodName);
      entityType = (Class<T>) ClassUtils
          .forName(serializedLambda.getImplClass().replace("/", "."), null);
      propertyType = entityType.getMethod(implMethodName).getReturnType();
      this.propertySetter = (obj, property) -> {

      };
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
      throw new ExcelException("解析属性名错误", e);
    }
    if (dateTimeFormatter == null && propertyType == Date.class) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(getCellFormat());
    }
    return this;
  }


  @SuppressWarnings("unchecked")
  public ExcelField<T, P> propertySetter(
      ExcelCellSetter<T, P> propertySetter) {
    this.propertySetter = propertySetter;
    if (propertyName == null) {
      try {
        Method writeReplace = propertySetter.getClass().getDeclaredMethod("writeReplace");
        writeReplace.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(propertySetter);
        String implMethodName = serializedLambda.getImplMethodName();
        propertyName = resolvePropertyName(implMethodName);
        entityType = (Class<T>) ClassUtils
            .forName(serializedLambda.getImplClass().replace("/", "."), null);
        Method[] methods = entityType.getMethods();
        for (Method method : methods) {
          if (method.getName().equals(implMethodName)) {
            propertyType = method.getParameterTypes()[0];
            break;
          }
        }
        if (propertyType == null) {
          throw new ExcelException("解析属性类型失败", null);
        }
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
        throw new ExcelException("解析属性名错误", e);
      }
    }
    if (dateTimeFormatter == null && propertyType == Date.class) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(getCellFormat());
    }
    return this;
  }

  public ExcelField<T, P> title(String title) {
    this.title = title;
    return this;
  }

  public ExcelField<T, P> comment(String comment) {
    this.comment = comment;
    return this;
  }

  public ExcelField<T, P> defaultValue(P defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public ExcelField<T, P> pattern(String pattern) {
    this.pattern = pattern;
    return this;
  }

  public ExcelField<T, P> align(Alignment align) {
    this.align = align;
    return this;
  }

  public ExcelField<T, P> width(double width) {
    this.width = width;
    return this;
  }
}
