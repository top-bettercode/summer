package top.bettercode.summer.tools.sap.connection;


import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRecord;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import top.bettercode.summer.logging.annotation.LogMarker;
import top.bettercode.summer.tools.lang.operation.PrettyPrintingContentModifier;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;
import top.bettercode.summer.tools.sap.annotation.SapStructure;
import top.bettercode.summer.tools.sap.annotation.SapTable;
import top.bettercode.summer.tools.sap.config.SapProperties;
import top.bettercode.summer.tools.sap.connection.pojo.ISapReturn;

@LogMarker(SapService.LOG_MARKER_STRING)
@SuppressWarnings("unchecked")
public class SapService {

  public static final String LOG_MARKER_STRING = "sap";
  private final static Marker LOG_MARKER = MarkerFactory.getMarker(LOG_MARKER_STRING);
  private final Logger log = LoggerFactory.getLogger(SapService.class);

  private final static String ABAP_AS_POOLED = "ABAP_AS_WITH_POOL";

  private boolean filterNonAnnField = true;
  private boolean filterNullFiled = false;

  public SapService(SapProperties properties) {
    DestinationDataProviderImpl destDataProvider = new DestinationDataProviderImpl();
    destDataProvider.addDestinationProperties(ABAP_AS_POOLED, loadProperties(properties));
    Environment.registerDestinationDataProvider(destDataProvider);
  }

  private Properties loadProperties(SapProperties properties) {
    Properties props = new Properties();
    props.setProperty(DestinationDataProvider.JCO_USER, properties.getUser());
    props.setProperty(DestinationDataProvider.JCO_PASSWD, properties.getPasswd());
    props.setProperty(DestinationDataProvider.JCO_LANG, properties.getLang());
    props.setProperty(DestinationDataProvider.JCO_CLIENT, properties.getClient());
    props.setProperty(DestinationDataProvider.JCO_SYSNR, properties.getSysnr());
    props.setProperty(DestinationDataProvider.JCO_ASHOST, properties.getAshost());
    props.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, properties.getPeakLimit());
    props.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, properties.getPoolCapacity());
    return props;
  }


  private JCoDestination getDestination() throws JCoException {
    return JCoDestinationManager.getDestination(ABAP_AS_POOLED);
  }

  public JCoFunction getFunction(String functionName) throws JCoException {
    return getDestination().getRepository().getFunction(functionName);
  }

  public SapService setFilterNonAnnField(boolean filterNonAnnField) {
    this.filterNonAnnField = filterNonAnnField;
    return this;
  }

  public SapService setFilterNullFiled(boolean filterNullFiled) {
    this.filterNullFiled = filterNullFiled;
    return this;
  }

  public <T extends ISapReturn> T invoke(String functionName, Object data,
      Class<T> returnClass) {
    return invoke(functionName, data, returnClass, true);
  }

  public <T extends ISapReturn> T invoke(String functionName, Object data,
      Class<T> returnClass,
      boolean checkError) {
    JCoFunction function = null;
    T result;
    Throwable throwable = null;
    boolean isSuccess = true;
    long start = System.currentTimeMillis();
    Long durationMillis = null;
    try {
      JCoDestination destination = getDestination();
      function = destination.getRepository().getFunction(functionName);
      Assert.notNull(function, functionName + " function not found");
      if (data != null) {
        this.parseInputParamObject(function, data);
      }

      function.execute(destination);

      JCoRecord out;
      if (returnClass.isAnnotationPresent(SapStructure.class)) {
        SapStructure struAnn = returnClass.getAnnotation(SapStructure.class);
        out = function.getExportParameterList().getStructure(struAnn.value());
      } else {
        out = function.getExportParameterList();
      }
      result = this.toBean(function, out, returnClass);

      durationMillis = System.currentTimeMillis() - start;
      if (!checkError || result.isOk()) {
        isSuccess = result.isSuccess();
        return result;
      } else {
        String msgText = result.getMessage();
        throw new SapSysException(StringUtils.hasText(msgText) ? msgText : "RFC请求失败");
      }
    } catch (Exception e) {
      throwable = e;
      if (e instanceof SapException) {
        throw (SapException) e;
      } else if (e instanceof SapSysException) {
        throw (SapSysException) e;
      } else {
        String message = e.getMessage();
        String msgRegex = "^Integer '(.*?)' has to many digits at field (.*?)$";
        if (message.matches(msgRegex)) {
          String fieldValue = message.replaceAll(msgRegex, "$1");
          String fieldName = message.replaceAll(msgRegex, "$2");
          JCoField jCoField = getField(Objects.requireNonNull(function).getImportParameterList(),
              fieldName);
          if (jCoField != null) {
            fieldName = jCoField.getDescription();
            int length = jCoField.getLength();
            message = String.format("%s的长度为%d，\"%s\"超出长度限制", fieldName, length,
                fieldValue);
          } else {
            message = String.format("%s超出长度限制", fieldValue);
          }
        }
        throw new SapException(message, e);
      }
    } finally {
      if (durationMillis == null) {
        durationMillis = System.currentTimeMillis() - start;
      }
      String exception = printException(function);
      if (!StringUtils.hasText(exception) && throwable != null) {
        exception = StringUtil.valueOf(throwable, true);
      }
      if (throwable != null || !isSuccess) {
        if (log.isWarnEnabled()) {
          log.warn(LOG_MARKER, "\nDURATION MILLIS : {}\n{}\n{}", durationMillis,
              printFunctionList(function),
              exception);
        }
      } else if (log.isInfoEnabled()) {
        log.info(LOG_MARKER, "\nDURATION MILLIS : {}\n{}\n{}", durationMillis,
            printFunctionList(function),
            exception);
      }
    }
  }

  private JCoField getField(Iterable<JCoField> jCoFields, String fieldName) {
    for (JCoField jCoField : jCoFields) {
      if (jCoField.isTable()) {
        JCoField field = getField(jCoField.getTable(), fieldName);
        if (field != null) {
          return field;
        }
      } else if (jCoField.isStructure()) {
        JCoField field = getField(jCoField.getStructure(), fieldName);
        if (field != null) {
          return field;
        }
      } else {
        String name = jCoField.getName();
        if (name.equals(fieldName)) {
          return jCoField;
        }
      }
    }
    return null;
  }

  private String printFunctionList(JCoFunction function) {
    if (function == null) {
      return "";
    }
    String xml = function.toXML();
    return PrettyPrintingContentModifier.modifyContent(xml);
  }

  private String printException(JCoFunction function) {
    if (function == null) {
      return "";
    }
    AbapException[] exceptionList = function.getExceptionList();
    StringBuilder sb = new StringBuilder();
    if (exceptionList != null) {
      for (AbapException exception : exceptionList) {
        sb.append(StringUtil.valueOf(exception, true)).append("\n");
      }
    }
    return sb.toString();
  }

  private void parseInputParamObject(JCoFunction function, Object data)
      throws IllegalAccessException, IllegalArgumentException {
    JCoParameterList input = function.getImportParameterList();
    Field[] fields = data.getClass().getDeclaredFields();
    String fieldName;

    BeanWrapperImpl beanWrapper = new BeanWrapperImpl(data);
    for (Field field : fields) {
      Object fv = beanWrapper.getPropertyValue(field.getName());
      fieldName = field.getName();
      if (fv == null && this.filterNullFiled) {
        log.info("Not Setting SAP param: " + fieldName + " is " + fv);
      } else if (field.isAnnotationPresent(SapStructure.class)) {
        SapStructure struAnn = field.getAnnotation(SapStructure.class);
        JCoStructure struJco = input.getStructure(struAnn.value());
        Map<String, Object> map = this.toSapParamMap(fv);
        if (map != null && !map.isEmpty()) {
          for (Entry<String, Object> entry : map.entrySet()) {
            struJco.setValue(entry.getKey(), entry.getValue());
          }
        }
      } else if (field.isAnnotationPresent(SapTable.class)) {
        SapTable sapTable = field.getAnnotation(SapTable.class);
        JCoParameterList tableParameterList = function.getTableParameterList();
        Assert.notNull(tableParameterList,
            function.getName() + " function Table Parameter List not found");
        JCoTable table = tableParameterList.getTable(sapTable.value());
        if (fv instanceof List) {
          List<Object> objList = (List<Object>) fv;
          for (Object obj : objList) {
            table.appendRow();
            Map<String, Object> map;
            if (obj instanceof Map) {
              map = (Map<String, Object>) obj;
            } else {
              map = this.toSapParamMap(obj);
            }
            if (map != null && !map.isEmpty()) {
              for (Entry<String, Object> entry : map.entrySet()) {
                table.setValue(entry.getKey(), entry.getValue());
              }
            }
          }
        }
      } else if (field.isAnnotationPresent(SapField.class)) {
        SapField sapField = field.getAnnotation(SapField.class);
        if (sapField != null && sapField.value() != null) {
          fieldName = sapField.value();
        }
        input.setValue(fieldName, fv);
      } else if (!this.filterNonAnnField) {
        input.setValue(fieldName, fv);
      }
    }

  }

  private <T> T toBean(JCoFunction function, JCoRecord out, Class<T> returnClass)
      throws InstantiationException, IllegalAccessException, IntrospectionException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
    T result = returnClass.getDeclaredConstructor().newInstance();
    BeanWrapperImpl beanWrapper = new BeanWrapperImpl(result);
    ReflectionUtils.doWithFields(returnClass, field -> {
      try {
        String fieldName = field.getName();
        Class<?> propertyType = Objects.requireNonNull(
            beanWrapper.getPropertyTypeDescriptor(fieldName)).getType();
        if (field.isAnnotationPresent(SapStructure.class)) {
          SapStructure struAnn = field.getAnnotation(SapStructure.class);
          Object fieldObj = this.toBean(function, out.getStructure(struAnn.value()), propertyType);
          beanWrapper.setPropertyValue(fieldName, fieldObj);
        } else if (field.isAnnotationPresent(SapTable.class)) {
          SapTable tabAnn = field.getAnnotation(SapTable.class);
          JCoTable tb = function.getTableParameterList().getTable(tabAnn.value());
          beanWrapper.setPropertyValue(fieldName, this.toList(tb, field.getGenericType()));
        } else if (field.isAnnotationPresent(SapField.class)) {
          SapField sapField = field.getAnnotation(SapField.class);
          if (sapField != null && sapField.value() != null) {
            fieldName = sapField.value();
            beanWrapper.setPropertyValue(field.getName(), out.getValue(fieldName));
          }
        } else if (!this.filterNonAnnField) {
          beanWrapper.setPropertyValue(fieldName, out.getValue(fieldName));
        }
      } catch (IntrospectionException | InstantiationException | InvocationTargetException |
               NoSuchMethodException e) {
        throw new SapException(e);
      }
    });

    return result;
  }

  private <T> List<T> toList(JCoTable table, Type classType)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException, NoSuchMethodException {
    List<T> tableList = new ArrayList<>();
    Type[] params = ((ParameterizedType) classType).getActualTypeArguments();
    Class<?> elementClass = (Class<?>) params[0];
    T element = null;

    for (int i = 0; i < table.getNumRows(); ++i) {
      table.setRow(i);
      if (element instanceof Map) {
        Map<String, Object> row = new HashMap<>();
        for (int j = 0; j < table.getNumRows(); ++j) {
          for (JCoField field : table) {
            row.put(field.getName(), field.getValue());
          }
        }
        element = (T) row;
      } else {
        element = (T) elementClass.getDeclaredConstructor().newInstance();
        Field[] fields = element.getClass().getDeclaredFields();
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(element);
        for (JCoField fld : table) {
          for (Field field : fields) {
            String fieldName = field.getName();
            if (field.isAnnotationPresent(SapField.class)) {
              fieldName = field.getAnnotation(SapField.class).value();
            }
            if (fieldName.equals(fld.getName())) {
              beanWrapper.setPropertyValue(field.getName(), fld.getValue());
              break;
            }
          }
        }
      }
      tableList.add(element);
    }
    return tableList;
  }

  private Map<String, Object> toSapParamMap(Object obj)
      throws IllegalAccessException, IllegalArgumentException {
    if (obj == null) {
      return null;
    }
    Map<String, Object> sapObjMap = new HashMap<>();
    Field[] fields = obj.getClass().getDeclaredFields();

    BeanWrapperImpl beanWrapper = new BeanWrapperImpl(obj);
    for (Field field : fields) {
      Object fv;
      try {
        fv = beanWrapper.getPropertyValue(field.getName());
      } catch (Exception var12) {
        field.setAccessible(true);
        fv = field.get(obj);
      }
      if (fv != null || !this.filterNullFiled) {
        String fieldName = field.getName();
        if (field.isAnnotationPresent(SapField.class)) {
          SapField sapField = field.getAnnotation(SapField.class);
          if (sapField != null && sapField.value() != null) {
            fieldName = sapField.value();
            sapObjMap.put(fieldName, fv);
          }
        }
        if (!this.filterNonAnnField) {
          sapObjMap.put(fieldName, fv);
        }
      }
    }
    return sapObjMap;
  }

}
