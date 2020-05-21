package cn.bestwu.simpleframework.util.excel;

import cn.bestwu.lang.util.BooleanUtil;
import cn.bestwu.lang.util.LocalDateTimeHelper;
import cn.bestwu.lang.util.MoneyUtil;
import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Excel 字段定义
 */
public class ExcelField<T, P> {

  /**
   * 导出字段标题
   */
  private final String title;

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
  private ExcelConverter<T, P> fieldConverter;

  /**
   * 单元格值转属性字段值
   */
  private ExcelConverter<String, Object> propertyConverter;


  /**
   * 属性字段值转单元格值
   */
  private ExcelConverter<P, Object> cellConverter;

  /**
   * 单元格值设置实体属性
   */
  private ExcelCellSetter<T, P> propertySetter;

  /**
   * 实体类型
   */
  protected Class<T> entityType;
  /**
   * 属性字段类型
   */
  protected Class<?> propertyType;
  /**
   * 属性字段名称
   */
  protected String propertyName;
  /**
   * 日期格式
   */
  protected DateTimeFormatter dateTimeFormatter;

  //--------------------------------------------
  public static <T, P> ExcelField<T, P> of(String title, ExcelConverter<T, P> fieldConverter) {
    return new ExcelField<>(title, fieldConverter);
  }
  //--------------------------------------------

  public ExcelField<T, P> yuan() {
    return cell((property) -> MoneyUtil.toYun((Long) property).toString())
        .property(MoneyUtil::toCent);
  }

  public ExcelField<T, P> millis() {
    return cell((property) -> LocalDateTimeHelper.of((Long) property).format(dateTimeFormatter))
        .property((cellValue) -> LocalDateTimeHelper
            .of(DateUtil.getJavaDate(Double.parseDouble(cellValue))).toMillis());
  }


  public ExcelField<T, P> bool() {
    return cell((property) -> (Boolean) property ? "是" : "否").property(BooleanUtil::toBoolean);
  }


  public ExcelField<T, P> code() {
    return code(propertyName);
  }

  public ExcelField<T, P> code(String codeType) {
    return cell((property) -> CodeSerializer.getName(codeType, (Serializable) property)).property(
        (cellValue) -> getCode(codeType, cellValue));
  }


  public ExcelField<T, P> stringCode() {
    return stringCode(propertyName);
  }

  public ExcelField<T, P> stringCode(String codeType) {
    return cell((property) -> {
      String code = (String) property;
      if (code.contains(",")) {
        String[] split = code.split(",");
        return StringUtils.arrayToCommaDelimitedString(
            Arrays.stream(split).map(s -> CodeSerializer.getName(codeType, s.trim()))
                .toArray());
      } else {
        return CodeSerializer.getName(codeType, code);
      }
    }).property((cellValue) -> getCode(codeType, cellValue));
  }

  //--------------------------------------------

  private Object getCode(String codeType, String cellValue) {
    if (cellValue.contains(",")) {
      String[] split = cellValue.split(",");
      return StringUtils.arrayToCommaDelimitedString(
          Arrays.stream(split).map(s -> CodeSerializer.getCode(codeType, s.trim()))
              .toArray());
    } else {
      return CodeSerializer.getCode(codeType, cellValue);
    }
  }

  //--------------------------------------------

  public ExcelField<T, P> getter(ExcelConverter<T, P> cellConverter) {
    this.fieldConverter = cellConverter;
    return this;
  }

  public ExcelField<T, P> setter(ExcelCellSetter<T, P> propertySetter) {
    this.propertySetter = propertySetter;
    return this;
  }

  public ExcelField<T, P> property(ExcelConverter<String, Object> propertyConverter) {
    this.propertyConverter = propertyConverter;
    return this;
  }

  public ExcelField<T, P> cell(ExcelConverter<P, Object> cellConverter) {
    this.cellConverter = cellConverter;
    return this;
  }

  //--------------------------------------------
  @SuppressWarnings("unchecked")
  private ExcelField(String title, ExcelConverter<T, P> fieldConverter) {
    this.title = title;
    this.fieldConverter = fieldConverter;
    try {
      Method writeReplace = fieldConverter.getClass().getDeclaredMethod("writeReplace");
      writeReplace.setAccessible(true);
      SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(fieldConverter);
      String implMethodName = serializedLambda.getImplMethodName();
      propertyName = resolvePropertyName(implMethodName);
      entityType = (Class<T>) ClassUtils
          .forName(serializedLambda.getImplClass().replace("/", "."), null);
      propertyType = entityType.getMethod(implMethodName).getReturnType();
      Method writeMethod = entityType
          .getMethod("set" + StringUtils.capitalize(propertyName), propertyType);

      this.propertySetter = (obj, property) -> ReflectionUtils
          .invokeMethod(writeMethod, obj, property);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
      throw new ExcelException("属性解析错误", e);
    }
    if (dateTimeFormatter == null && propertyType == Date.class) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(getCellFormat());
    }

    propertyConverter = (cellValue) -> {
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
    };

    this.cellConverter = (property) -> {
      if (property == null) {
        return "";
      } else if (propertyType.equals(BigDecimal.class)) {
        return ((BigDecimal) property).toPlainString();
      } else {
        return property;
      }
    };
  }

  //--------------------------------------------

  /**
   * @param obj 实体对象
   * @return 单元格值
   */
  public Object toCellValue(T obj) {
    P property = fieldConverter.convert(obj);
    if (property == null) {
      property = defaultValue;
    }
    return cellConverter.convert(property);
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
        property = (P) propertyConverter.convert(cellValue);
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
   * @return 单元格格式
   */
  public String getCellFormat() {
    String cellFormatString = pattern;
    if (!StringUtils.hasText(cellFormatString)) {
      if (propertyType.equals(Integer.class)) {
        return "0";
      } else if (propertyType.equals(Long.class)) {
        return "0";
      } else if (propertyType.equals(BigDecimal.class)) {
        return "0.00";
      } else if (propertyType.equals(Double.class)) {
        return "0.00";
      } else if (propertyType.equals(Float.class)) {
        return "0.00";
      } else if (propertyType.equals(Date.class)) {
        return "yyyy-MM-dd HH:mm";
      }
      return "@";
    } else {
      return cellFormatString;
    }
  }

  private String resolvePropertyName(String getMethodName) {
    if (getMethodName.startsWith("get")) {
      getMethodName = getMethodName.substring(3);
    } else if (getMethodName.startsWith("is")) {
      getMethodName = getMethodName.substring(2);
    }
    return StringUtils.uncapitalize(getMethodName);
  }

  //--------------------------------------------

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

}
