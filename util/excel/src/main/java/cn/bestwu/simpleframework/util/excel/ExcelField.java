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
 * Excel字段描述
 *
 * @param <P> 属性类型
 * @param <T> 实体类型
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
  private String pattern;

  /**
   * 导出字段水平对齐方式
   * <p>
   * Define horizontal alignment.
   * <a href="https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.horizontalalignmentvalues(v=office.14).aspx">here</a>.
   */
  private Alignment align = Alignment.center;

  /**
   * 列宽度，-1表示自动计算
   */
  private double width = -1;

  /**
   * 获取实体属性
   */
  private ExcelConverter<T, P> propertyGetter;

  /**
   * 设置实体属性
   */
  private ExcelCellSetter<T, P> propertySetter;

  /**
   * 单元格值转属性字段值
   */
  private ExcelConverter<String, Object> propertyConverter;

  /**
   * 属性字段值转单元格值
   */
  private ExcelConverter<P, Object> cellConverter;

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

  /**
   * 只支持导入的初始化方法
   *
   * @param <P>            属性类型
   * @param <T>            实体类型
   * @param title          标题
   * @param propertyType   属性字段类型
   * @param propertySetter 属性设置方法
   * @return Excel字段描述
   */
  public static <T, P> ExcelField<T, P> of(String title, Class<P> propertyType,
      ExcelCellSetter<T, P> propertySetter) {
    return new ExcelField<>(title, propertyType, propertySetter);
  }

  /**
   * 只支持导出的初始化方法
   *
   * @param <P>            属性类型
   * @param <T>            实体类型
   * @param title          标题
   * @param propertyType   属性字段类型
   * @param propertyGetter 属性获取方法
   * @return Excel字段描述
   */
  public static <T, P> ExcelField<T, P> of(String title, Class<P> propertyType,
      ExcelConverter<T, P> propertyGetter) {
    return new ExcelField<>(title, propertyType, propertyGetter);
  }


  /**
   * 支持导入及导出的初始化方法
   *
   * @param <P>            属性类型
   * @param <T>            实体类型
   * @param title          标题
   * @param propertyType   属性字段类型
   * @param propertyGetter 属性获取方法
   * @param propertySetter 属性设置方法
   * @return Excel字段描述
   */
  public static <T, P> ExcelField<T, P> of(String title, Class<P> propertyType,
      ExcelConverter<T, P> propertyGetter, ExcelCellSetter<T, P> propertySetter) {
    return new ExcelField<>(title, propertyType, propertyGetter).setter(propertySetter);
  }


  /**
   * 支持导入及导出的初始化方法
   *
   * @param <P>            属性类型
   * @param <T>            实体类型
   * @param title          标题
   * @param propertyGetter 属性获取方法
   * @return Excel字段描述
   */
  public static <T, P> ExcelField<T, P> of(String title, ExcelConverter<T, P> propertyGetter) {
    return new ExcelField<>(title, propertyGetter);
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

  public ExcelField<T, P> getter(ExcelConverter<T, P> propertyGetter) {
    this.propertyGetter = propertyGetter;
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
  private ExcelField(String title, ExcelConverter<T, P> propertyGetter) {
    this.title = title;
    this.propertyGetter = propertyGetter;
    try {
      Method writeReplace = propertyGetter.getClass().getDeclaredMethod("writeReplace");
      writeReplace.setAccessible(true);
      SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(propertyGetter);
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

    init();
  }

  /**
   * 只支持导出的初始化方法
   *
   * @param title          标题
   * @param propertyType   属性字段类型
   * @param propertyGetter 属性获取方法
   */
  private ExcelField(String title, Class<P> propertyType, ExcelConverter<T, P> propertyGetter) {
    this.title = title;
    this.propertyType = propertyType;
    this.propertyGetter = propertyGetter;

    init();
  }


  /**
   * 只支持导入的初始化方法
   *
   * @param title          标题
   * @param propertyType   属性字段类型
   * @param propertySetter 属性设置方法
   */
  private ExcelField(String title, Class<P> propertyType, ExcelCellSetter<T, P> propertySetter) {
    this.title = title;
    this.propertyType = propertyType;
    this.propertySetter = propertySetter;

    init();
  }


  private void init() {
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

    if (this.pattern == null) {
      if (propertyType.equals(Integer.class)) {
        this.pattern = "0";
      } else if (propertyType.equals(Long.class)) {
        this.pattern = "0";
      } else if (propertyType.equals(BigDecimal.class)) {
        this.pattern = "0.00";
      } else if (propertyType.equals(Double.class)) {
        this.pattern = "0.00";
      } else if (propertyType.equals(Float.class)) {
        this.pattern = "0.00";
      } else if (propertyType.equals(Date.class)) {
        this.pattern = "yyyy-MM-dd HH:mm";
      } else {
        this.pattern = "@";
      }
    }

    if (dateTimeFormatter == null && propertyType == Date.class) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
    }
  }

  //--------------------------------------------

  /**
   * @param obj 实体对象
   * @return 单元格值
   */
  public Object toCellValue(T obj) {
    P property = propertyGetter.convert(obj);
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
    } catch (Exception e) {
      String message = e.getMessage();
      throw new IllegalArgumentException(StringUtils.hasText(message) ? message : "typeMismatch",
          e);
    }
    if (propertyName != null) {
      Set<ConstraintViolation<Object>> constraintViolations = validator
          .validateProperty(obj, propertyName, validateGroups);
      if (!constraintViolations.isEmpty()) {
        throw new ConstraintViolationException(constraintViolations);
      }
    }
  }

  //--------------------------------------------

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

  public String pattern() {
    return pattern;
  }

  public Alignment align() {
    return align;
  }

  public double width() {
    return width;
  }

}
