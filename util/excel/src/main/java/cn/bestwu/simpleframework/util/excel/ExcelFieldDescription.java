package cn.bestwu.simpleframework.util.excel;

import cn.bestwu.lang.util.LocalDateTimeHelper;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 * @since 0.1.12
 */
public class ExcelFieldDescription {

  private Logger log = LoggerFactory.getLogger(ExcelFieldDescription.class);
  private AccessibleObject accessibleObject;
  private Class<?> fieldType;
  private ExcelField excelField;
  private CellValueConverter cellValueConverter;
  private String[] propertyNames;
  private DateTimeFormatter dateTimeFormatter;

  public ExcelFieldDescription(ExcelField excelField, Class<?> fieldType,
      AccessibleObject accessibleObject) {
    this.fieldType = fieldType;
    this.accessibleObject = accessibleObject;
    this.excelField = excelField;
    if (fieldType.equals(Date.class)) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(getCellFormat());
    }
    Class<? extends CellValueConverter> converter = excelField.converter();
    if (converter != CellValueConverter.class) {
      try {
        cellValueConverter = (CellValueConverter) converter.getMethod("newInstance").invoke(null);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException e1) {
        try {
          cellValueConverter = converter.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
          throw new ExcelException("单元格转换器初始化失败", e);
        }
      }
    }
    if (StringUtils.hasText(excelField.value())) {
      propertyNames = Arrays.stream(StringUtils.split(excelField.value(), ".")).map(
          (Function<String, Object>) StringUtils::capitalize).toArray(String[]::new);
    }
  }


  /**
   * @param o 实体对象
   * @return field value
   */
  public Object read(Object o) {
    Object fieldValue = excelField.defaultValue();
    try {
      if (propertyNames != null) {
        fieldValue = invokeGetter(o);
      } else if (accessibleObject instanceof Field) {
        fieldValue = (ReflectionUtils.invokeMethod(
            BeanUtils.getPropertyDescriptor(o.getClass(), ((Field) accessibleObject).getName())
                .getReadMethod(), o));
      } else if (accessibleObject instanceof Method) {
        fieldValue = (ReflectionUtils
            .invokeMethod(o.getClass().getMethod(((Method) accessibleObject).getName()), o));
      }
      if (fieldValue == null || fieldValue instanceof String && !StringUtils
          .hasText(String.valueOf(fieldValue))) {
        fieldValue = excelField.defaultValue();
      }
    } catch (NoSuchMethodException e) {
      log.warn("read value fail.", e);
    }
    return convert(fieldValue);
  }


  private Object convert(Object o) {
    if (cellValueConverter != null) {
      return o == null ? cellValueConverter.null2Cell(this, o)
          : cellValueConverter.toCell(o, this, o);
    } else if (o == null) {
      return "";
    } else if (fieldType.equals(BigDecimal.class)) {
      return ((BigDecimal) o).toPlainString();
    } else {
      return o;
    }
  }

  /**
   * @param cellValue      单元格值
   * @param o              实体对象
   * @param validator      验证器
   * @param validateGroups 验证组
   */
  public void write(Object o, String cellValue,
      Validator validator,
      Class<?>... validateGroups) {
    try {
      if (!StringUtils.hasText(cellValue)) {
        cellValue = excelField.defaultInValue();
      }

      if (cellValueConverter != null) {
        setField(o,
            (StringUtils.hasText(cellValue) ? cellValueConverter.fromCell(cellValue, this, o)
                : cellValueConverter.emptyfromCell(this, o)), validator,
            validateGroups);
      } else {
        if (!StringUtils.hasText(cellValue)) {
          setField(o, null, validator, validateGroups);
        } else if (fieldType == String.class) {
          setField(o, cellValue, validator, validateGroups);
        } else if (fieldType == Integer.class) {
          setField(o, Double.valueOf(cellValue).intValue(), validator, validateGroups);
        } else if (fieldType == Long.class) {
          setField(o, Double.valueOf(cellValue).longValue(), validator, validateGroups);
        } else if (fieldType == BigDecimal.class) {
          setField(o, new BigDecimal(cellValue), validator, validateGroups);
        } else if (fieldType == Double.class) {
          setField(o, Double.valueOf(cellValue), validator, validateGroups);
        } else if (fieldType == Float.class) {
          setField(o, Float.valueOf(cellValue), validator, validateGroups);
        } else if (fieldType == Date.class) {
          Date date;
          try {
            date = DateUtil.getJavaDate(Double.parseDouble(cellValue));
          } catch (NumberFormatException e) {
            date = LocalDateTimeHelper.parse(cellValue, dateTimeFormatter).toDate();
          }
          setField(o, date, validator, validateGroups);
        } else {
          throw new IllegalArgumentException("类型不正确,期待的数据类型:" + fieldType.getName());
        }
      }
    } catch (Exception ex) {
      String message = ex.getMessage();
      throw new IllegalArgumentException(StringUtils.hasText(message) ? message : "typeMismatch",
          ex);
    }
  }


  /**
   * @return 单元格格式
   */
  public String getCellFormat() {
    String cellFormatString = excelField.pattern();
    if (!StringUtils.hasText(cellFormatString)) {
      cellFormatString = getCellFormat(fieldType);
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

  /**
   * @param o              实体对象
   * @param val            字段值
   * @param validator      验证器
   * @param validateGroups 验证组
   * @throws NoSuchMethodException NoSuchMethodException
   */
  private void setField(Object o, Object val, Validator validator, Class<?>[] validateGroups)
      throws NoSuchMethodException {
    String propertyName = null;
    if (accessibleObject instanceof Field) {
      propertyName = ((Field) accessibleObject).getName();
      if (val == null && ((Field) accessibleObject).getType().equals(String.class)) {
        val = "";
      }
      ReflectionUtils.invokeMethod(
          BeanUtils.getPropertyDescriptor(o.getClass(), propertyName)
              .getWriteMethod(), o, val);
    } else if (accessibleObject instanceof Method) {
      String mthodName = ((Method) accessibleObject).getName();
      if ("get".equals(mthodName.substring(0, 3))) {
        propertyName = mthodName.substring(3);
        mthodName = "set" + propertyName;
        propertyName = StringUtils.uncapitalize(propertyName);
      }
      if (val == null && ((Method) accessibleObject).getReturnType().equals(String.class)) {
        val = "";
      }
      ReflectionUtils.invokeMethod(o.getClass().getMethod(mthodName, fieldType), o, val);
    }
    Set<ConstraintViolation<Object>> constraintViolations = validator
        .validateProperty(o, propertyName, validateGroups);
    if (!constraintViolations.isEmpty()) {
      throw new ConstraintViolationException(constraintViolations);
    }
  }


  /**
   * 调用Getter方法. 支持多级，如：对象名.对象名.方法
   */
  private Object invokeGetter(Object obj) throws NoSuchMethodException {
    Object object = obj;
    for (String name : propertyNames) {
      String getterMethodName = "get" + name;
      object = ReflectionUtils.invokeMethod(object.getClass().getMethod(getterMethodName), object);
    }
    return object;
  }

  //--------------------------------------------

  public String title() {
    return excelField.title();
  }

  public String comment() {
    return excelField.comment();
  }

  public DateTimeFormatter getDateTimeFormatter() {
    return dateTimeFormatter;
  }

  public AccessibleObject getAccessibleObject() {
    return accessibleObject;
  }

  public Class<?> getFieldType() {
    return fieldType;
  }

  public ExcelField getExcelField() {
    return excelField;
  }
}
