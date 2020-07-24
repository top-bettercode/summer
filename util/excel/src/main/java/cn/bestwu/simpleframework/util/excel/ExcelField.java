package cn.bestwu.simpleframework.util.excel;

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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
   * 默认空值
   */
  private String nullValue = "";

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
   * 是否需要合并
   */
  private boolean merge = false;
  /**
   * mergeId列，此列不导出，用于判断是否合并之前相同mergeId的行
   */
  private boolean mergeId = false;

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

  public static <T, P> ExcelField<T, P> mergeId(ExcelConverter<T, P> propertyGetter) {
    return new ExcelField<>("", propertyGetter).mergeId();
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

  public ExcelField<T, P> property(ExcelConverter<String, Object> propertyConverter) {
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


  private void init() {
    propertyConverter = (cellValue) -> {
      try {
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
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("类型不正确,期待输入数字类型");
      }

      throw new IllegalArgumentException("不支持的数据类型:" + propertyType.getName());
    };

    cellConverter = (property) -> {
      if (property == null) {
        return nullValue;
      } else if (propertyType.equals(String.class) || ClassUtils.isPrimitiveOrWrapper(propertyType)
          || propertyType.equals(Date.class)) {
        return property;
      } else if (propertyType.equals(BigDecimal.class)) {
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


  private String resolvePropertyName(String methodName) {
    if (methodName.startsWith("get")) {
      methodName = methodName.substring(3);
    } else if (methodName.startsWith("is")) {
      methodName = methodName.substring(2);
    }
    return StringUtils.uncapitalize(methodName);
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

  /**
   * 是否需要合并
   *
   * @return ExcelField
   */
  public ExcelField<T, P> merge() {
    this.merge = true;
    return this;
  }

  /**
   * mergeId列，此列不导出，用于判断是否合并之前相同mergeId的行
   *
   * @return ExcelField
   */
  private ExcelField<T, P> mergeId() {
    this.merge = true;
    this.mergeId = true;
    return this;
  }

  //--------------------------------------------

  /**
   * @param obj 实体对象
   * @return 单元格值
   */
  Object toCellValue(T obj) {
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
  void setProperty(T obj, String cellValue, Validator validator,
      Class<?>[] validateGroups) {
    P property;
    if (!StringUtils.hasText(cellValue)) {
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

  boolean isMergeId() {
    return mergeId;
  }
}
