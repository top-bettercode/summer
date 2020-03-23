package cn.bestwu.simpleframework.util.excel;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * @author Peter Wu
 * @since 0.0.1
 */
public abstract class AbstractExcelUtil {

  private static final Map<String, List<ExcelFieldDescription>> caches = new ConcurrentReferenceHashMap<>(
      256);

  public List<ExcelFieldDescription> getExcelFieldDescriptions(Class<?> cls, ExcelFieldType type) {
    String key = cls.toGenericString() + ":" + type.name();
    return caches.computeIfAbsent(key, k -> {
      List<ExcelFieldDescription> fieldDescriptions = new ArrayList<>();
      // Get annotation field
      Field[] fields = cls.getDeclaredFields();
      for (Field f : fields) {
        readExcelField(fieldDescriptions, f, type, f.getType());
      }
      // Get annotation method
      Method[] methods = cls.getMethods();
      for (Method method : methods) {
        readExcelField(fieldDescriptions, method, type, method.getReturnType());
      }
      // Field sorting
      fieldDescriptions.sort(Comparator.comparingInt(o -> (o.getExcelField()).sort()));
      return fieldDescriptions;
    });
  }

  private void readExcelField(List<ExcelFieldDescription> fieldDescriptions,
      AccessibleObject accessibleObject, ExcelFieldType type, Class<?> fieldType) {
    ExcelField excelField = AnnotationUtils.findAnnotation(accessibleObject, ExcelField.class);
    if (excelField != null && (excelField.type().equals(ExcelFieldType.ALL) || excelField.type()
        .equals(type))) {
      fieldDescriptions.add(new ExcelFieldDescription(excelField, fieldType, accessibleObject));
    }
  }
}
