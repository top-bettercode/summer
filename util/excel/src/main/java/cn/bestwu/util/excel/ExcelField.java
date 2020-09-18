package cn.bestwu.util.excel;

import cn.bestwu.lang.util.BooleanUtil;
import cn.bestwu.lang.util.LocalDateTimeHelper;
import cn.bestwu.lang.util.MoneyUtil;
import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
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
   * 默认值
   */
  private P defaultValue;

  /**
   * 默认空值
   */
  private String nullValue = "";

  /**
   * 是否需要合并
   */
  private boolean merge = false;
  /**
   * 判断是否合并之前相同mergeGetter值的行
   */
  private ExcelConverter<T, P> mergeGetter;
  /**
   * 序号字段
   */
  private boolean indexColumn = false;
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
  private ExcelConverter<Object, Object> propertyConverter;

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
   * 支持导出(如果 propertyGetter 符合 ClassName::getProperty Lambda 签名，则可自动识别setter，支持导入)的初始化方法
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

  public static <T, P> ExcelField<T, P> index(String title) {
    ExcelField<T, P> excelField = new ExcelField<>(title);
    excelField.indexColumn();
    return excelField;
  }

  //--------------------------------------------

  public ExcelField<T, P> yuan() {
    return yuan(2);
  }

  public ExcelField<T, P> yuan(int scale) {
    return cell((property) -> MoneyUtil.toYun((Long) property, scale).toPlainString())
        .property(cent -> MoneyUtil.toCent((BigDecimal) cent));
  }

  public ExcelField<T, P> millis() {
    return millis(ExcelCell.DEFAULT_DATE_PATTERN);
  }

  public ExcelField<T, P> millis(String pattern) {
    this.pattern = pattern;
    this.dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
    return this;
  }

  public ExcelField<T, P> code() {
    Assert.hasText(propertyName, "属性名称未设置");
    return code(propertyName);
  }

  public ExcelField<T, P> code(String codeType) {
    return cell((property) -> {
      if (property instanceof String) {
        String code = String.valueOf(property);
        if (code.contains(",")) {
          String[] split = code.split(",");
          return StringUtils.arrayToCommaDelimitedString(
              Arrays.stream(split).map(s -> CodeSerializer.getName(codeType, s.trim()))
                  .toArray());
        } else {
          return CodeSerializer.getName(codeType, code);
        }
      } else {
        return CodeSerializer.getName(codeType, (Serializable) property);
      }
    }).property((cellValue) -> getCode(codeType, String.valueOf(cellValue)));
  }

  //--------------------------------------------

  private Object getCode(String codeType, String cellValue) {
    if (cellValue.contains(",")) {
      String[] split = cellValue.split(",");
      return StringUtils.arrayToCommaDelimitedString(
          Arrays.stream(split).map(s -> {
            Serializable code = CodeSerializer.getCode(codeType, s.trim());
            if (code == null) {
              throw new IllegalArgumentException("无\"" + s + "\"对应的类型");
            }
            return code;
          })
              .toArray());
    } else {
      Serializable code = CodeSerializer.getCode(codeType, cellValue);
      if (code == null) {
        throw new IllegalArgumentException("无\"" + cellValue + "\"对应的类型");
      }
      return code;
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

  public ExcelField<T, P> property(ExcelConverter<Object, Object> propertyConverter) {
    this.propertyConverter = propertyConverter;
    return this;
  }

  public ExcelField<T, P> cell(ExcelConverter<P, Object> cellConverter) {
    this.cellConverter = cellConverter;
    return this;
  }

  public ExcelField<T, P> none(String nullValue) {
    this.nullValue = nullValue;
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

      if (!implMethodName.contains("$new")) {
        entityType = (Class<T>) ClassUtils
            .forName(serializedLambda.getImplClass().replace("/", "."), null);

        try {
          propertyType = entityType.getMethod(implMethodName).getReturnType();
          try {
            propertyName = resolvePropertyName(implMethodName);
            Method writeMethod = entityType
                .getMethod("set" + StringUtils.capitalize(propertyName), propertyType);

            this.propertySetter = (obj, property) -> ReflectionUtils
                .invokeMethod(writeMethod, obj, property);
          } catch (NoSuchMethodException e) {
            Logger log = LoggerFactory.getLogger(ExcelField.class);
            log.info("自动识别属性setter方法失败");
            propertyName = null;
            propertySetter = null;
            entityType = null;
          }
        } catch (NoSuchMethodException e) {
          initPropertyType(serializedLambda);
          entityType = null;
        }
      } else {
        initPropertyType(serializedLambda);
      }
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
      throw new ExcelException(title + "属性解析错误", e);
    }

    init();
  }

  /**
   * 识别lambda方法返回类型
   *
   * @param serializedLambda SerializedLambda
   * @throws ClassNotFoundException ClassNotFoundException
   */
  private void initPropertyType(SerializedLambda serializedLambda) throws ClassNotFoundException {
    String implMethodSignature = serializedLambda.getImplMethodSignature();
    String returnTypeName = implMethodSignature.replaceAll("^.*\\)(.*?);?$", "$1")
        .replace("/", ".").replace("[L", "[");
    if (!returnTypeName.startsWith("[")) {
      returnTypeName = returnTypeName.substring(1);
    }
    propertyType = ClassUtils.forName(returnTypeName, ExcelField.class.getClassLoader());
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

  private ExcelField(String title) {
    this.title = title;
    this.pattern = ExcelCell.DEFAULT_PATTERN;
  }

  private void init() {
    Assert.notNull(propertyType, "propertyType 不能为空");
    if (this.pattern == null) {
      if (propertyType == Integer.class || propertyType == int.class) {
        this.pattern = "0";
      } else if (propertyType == Long.class || propertyType == long.class) {
        this.pattern = "0";
      } else if (propertyType.equals(BigDecimal.class)) {
        this.pattern = "0.00";
      } else if (propertyType == Double.class || propertyType == double.class) {
        this.pattern = "0.00";
      } else if (propertyType == Float.class || propertyType == float.class) {
        this.pattern = "0.00";
      } else if (propertyType == Date.class || propertyType == LocalDate.class
          || propertyType == LocalDateTime.class) {
        this.pattern = ExcelCell.DEFAULT_DATE_PATTERN;
      } else {
        this.pattern = ExcelCell.DEFAULT_PATTERN;
      }
    }

    if (dateTimeFormatter == null && propertyType == Date.class) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
    }

    propertyConverter = (cellValue) -> {
      if (propertyType == String.class) {
        return String.valueOf(cellValue);
      } else if (propertyType == boolean.class || propertyType == Boolean.class) {
        return cellValue instanceof Boolean ? cellValue
            : BooleanUtil.toBoolean(String.valueOf(cellValue));
      } else if (propertyType == Integer.class || propertyType == int.class) {
        return ((BigDecimal) cellValue).intValue();
      } else if (propertyType == Long.class || propertyType == long.class) {
        if (isDateField()) {
          if (cellValue instanceof LocalDateTime) {
            return LocalDateTimeHelper.of((LocalDateTime) cellValue).toMillis();
          } else {
            return LocalDateTimeHelper.parse(String.valueOf(cellValue), dateTimeFormatter)
                .toMillis();
          }
        } else {
          return ((BigDecimal) cellValue).longValue();
        }
      } else if (propertyType == BigDecimal.class) {
        return cellValue;
      } else if (propertyType == Double.class || propertyType == double.class) {
        return ((BigDecimal) cellValue).doubleValue();
      } else if (propertyType == Float.class || propertyType == float.class) {
        return ((BigDecimal) cellValue).floatValue();
      } else if (propertyType == Date.class) {
        if (cellValue instanceof LocalDateTime) {
          return LocalDateTimeHelper.of((LocalDateTime) cellValue).toDate();
        } else {
          return LocalDateTimeHelper.parse(String.valueOf(cellValue), dateTimeFormatter).toDate();
        }
      } else if (propertyType == LocalDateTime.class) {
        if (cellValue instanceof LocalDateTime) {
          return cellValue;
        } else {
          return LocalDateTimeHelper.parse(String.valueOf(cellValue), dateTimeFormatter)
              .toLocalDateTime();
        }
      } else if (propertyType == LocalDate.class) {
        if (cellValue instanceof LocalDateTime) {
          return LocalDateTimeHelper.of((LocalDateTime) cellValue).toLocalDate();
        } else {
          return LocalDateTimeHelper.parse(String.valueOf(cellValue), dateTimeFormatter)
              .toLocalDate();
        }
      }

      throw new IllegalArgumentException("不支持的数据类型:" + propertyType.getName());
    };

    cellConverter = (property) -> {
      if (propertyType == String.class || propertyType == Date.class) {
        return property;
      } else if (propertyType == boolean.class || propertyType == Boolean.class) {
        return (Boolean) property ? "是" : "否";
      } else if (isDateField() && (propertyType == Long.class || propertyType == long.class)) {
        return LocalDateTimeHelper.of((Long) property).format(dateTimeFormatter);
      } else if (ClassUtils.isPrimitiveOrWrapper(propertyType)) {
        return property;
      } else if (propertyType == BigDecimal.class) {
        return ((BigDecimal) property).toPlainString();
      } else if (propertyType.isArray()) {
        int length = Array.getLength(property);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
          if (i > 0) {
            buffer.append(",");
          }
          buffer.append(Array.get(property, i));
        }
        return buffer.toString();
      } else if (Collection.class.isAssignableFrom(propertyType)) {
        return StringUtils.collectionToCommaDelimitedString((Collection<?>) property);
      } else {
        return StringUtil.valueOf(property);
      }
    };


  }

  //--------------------------------------------


  private String resolvePropertyName(String methodName) {
    if (methodName.startsWith("get")) {
      methodName = methodName.substring(3);
    } else if (methodName.startsWith("is")) {
      methodName = methodName.substring(2);
    }
    return StringUtils.uncapitalize(methodName);
  }

  //--------------------------------------------

  public ExcelField<T, P> propertyName(String propertyName) {
    this.propertyName = propertyName;
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

  /**
   * 设为需要合并
   *
   * @param mergeGetter 以此获取的值为合并依据，连续相同的值自动合并
   * @return ExcelField
   */
  public ExcelField<T, P> mergeBy(ExcelConverter<T, P> mergeGetter) {
    this.merge = true;
    this.mergeGetter = mergeGetter;
    return this;
  }

  /**
   * 设为序列字段
   *
   * @return ExcelField
   */
  private ExcelField<T, P> indexColumn() {
    this.indexColumn = true;
    return this;
  }

  //--------------------------------------------

  /**
   * @param obj 实体对象
   * @return 单元格值
   */
  Object mergeId(T obj) {
    return mergeGetter.convert(obj);
  }

  /**
   * @param obj 实体对象
   * @return 单元格值
   */
  Object toCellValue(T obj) {
    P property = propertyGetter.convert(obj);
    if (property == null) {
      property = defaultValue;
    }
    if (property == null) {
      return nullValue;
    } else {
      return cellConverter.convert(property);
    }
  }

  /**
   * @param obj            实体对象
   * @param cellValue      单元格值
   * @param validator      参数验证
   * @param validateGroups 参数验证组
   */
  @SuppressWarnings("unchecked")
  void setProperty(T obj, Object cellValue, Validator validator,
      Class<?>[] validateGroups) {
    P property;
    if (isEmptyCell(cellValue)) {
      property = defaultValue;
    } else {
      property = (P) propertyConverter.convert(cellValue);
    }
    propertySetter.set(obj, property);
    if (propertyName != null) {
      Set<ConstraintViolation<Object>> constraintViolations = validator
          .validateProperty(obj, propertyName, validateGroups);
      if (!constraintViolations.isEmpty()) {
        throw new ConstraintViolationException(constraintViolations);
      }
    }
  }

  boolean isEmptyCell(Object cellValue) {
    return cellValue == null || (cellValue instanceof CharSequence && !StringUtils.hasText(
        (CharSequence) cellValue));
  }

  boolean isDateField() {
    return dateTimeFormatter != null;
  }

  //--------------------------------------------
  String title() {
    return title;
  }

  String comment() {
    return comment;
  }

  String pattern() {
    return pattern;
  }

  Alignment align() {
    return align;
  }

  double width() {
    return width;
  }

  boolean isMerge() {
    return merge;
  }

  boolean isIndexColumn() {
    return indexColumn;
  }
}
