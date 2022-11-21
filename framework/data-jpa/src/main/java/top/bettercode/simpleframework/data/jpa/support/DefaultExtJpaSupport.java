package top.bettercode.simpleframework.data.jpa.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Objects;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.data.annotation.LastModifiedDate;
import top.bettercode.simpleframework.data.jpa.SoftDelete;
import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;

/**
 * @author Peter Wu
 */
public class DefaultExtJpaSupport implements ExtJpaSupport {


  /**
   * 是否支持逻辑删除
   */
  private boolean supportSoftDeleted = false;
  private Class<?> softDeletedPropertyType;
  private Class<?> lastModifiedDatePropertyType;
  private Class<?> versionPropertyType;
  /**
   * 逻辑删除字段属性名.
   */
  private String softDeletedPropertyName;
  private String lastModifiedDatePropertyName;
  private String versionPropertyName;
  /**
   * 默认逻辑删除值.
   */
  private Object softDeletedTrueValue;
  /**
   * 默认逻辑未删除值.
   */
  private Object softDeletedFalseValue;
  private Method softDeletedReadMethod;
  private Method softDeletedWriteMethod;

  public <T> DefaultExtJpaSupport(JpaExtProperties jpaExtProperties, Class<?> domainClass)
      throws BeansException {
    String trueValue = null;
    String falseValue = null;
    int finish = 0;
    for (Field declaredField : domainClass.getDeclaredFields()) {
      if (softDeletedPropertyName == null) {
        SoftDelete annotation = declaredField.getAnnotation(SoftDelete.class);
        if (annotation != null) {
          trueValue = annotation.trueValue();
          falseValue = annotation.falseValue();
          softDeletedPropertyName = declaredField.getName();
          softDeletedPropertyType = declaredField.getType();
          finish++;
        }
      }
      if (lastModifiedDatePropertyName == null
          && declaredField.getAnnotation(LastModifiedDate.class) != null) {
        lastModifiedDatePropertyName = declaredField.getName();
        lastModifiedDatePropertyType = declaredField.getType();
        finish++;
      }
      if (versionPropertyName == null && declaredField.getAnnotation(Version.class) != null) {
        versionPropertyName = declaredField.getName();
        versionPropertyType = declaredField.getType();
        finish++;
      }
      if (finish == 3) {
        break;
      }
    }
    if (finish < 3 && domainClass.getSuperclass().isAnnotationPresent(MappedSuperclass.class)) {
      Class<?> descriptClass = domainClass.getSuperclass();
      for (Field declaredField : descriptClass.getDeclaredFields()) {
        if (softDeletedPropertyName == null) {
          SoftDelete annotation = declaredField.getAnnotation(SoftDelete.class);
          if (annotation != null) {
            trueValue = annotation.trueValue();
            falseValue = annotation.falseValue();
            softDeletedPropertyName = declaredField.getName();
            softDeletedPropertyType = declaredField.getType();
            finish++;
          }
        }
        if (lastModifiedDatePropertyName == null
            && declaredField.getAnnotation(LastModifiedDate.class) != null) {
          lastModifiedDatePropertyName = declaredField.getName();
          lastModifiedDatePropertyType = declaredField.getType();
          finish++;
        }
        if (versionPropertyName == null && declaredField.getAnnotation(Version.class) != null) {
          versionPropertyName = declaredField.getName();
          versionPropertyType = declaredField.getType();
          finish++;
        }
        if (finish == 3) {
          break;
        }
      }
    }

    if (finish < 3) {
      PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(domainClass);
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
        if (softDeletedPropertyName == null) {
          SoftDelete annotation = propertyDescriptor.getReadMethod()
              .getAnnotation(SoftDelete.class);
          if (annotation != null) {
            trueValue = annotation.trueValue();
            falseValue = annotation.falseValue();
            softDeletedPropertyName = propertyDescriptor.getName();
            softDeletedPropertyType = propertyDescriptor.getPropertyType();
            finish++;
          }
        }
        if (lastModifiedDatePropertyName == null
            && propertyDescriptor.getReadMethod().getAnnotation(LastModifiedDate.class) != null) {
          lastModifiedDatePropertyName = propertyDescriptor.getName();
          lastModifiedDatePropertyType = propertyDescriptor.getPropertyType();
          finish++;
        }
        if (versionPropertyName == null
            && propertyDescriptor.getReadMethod().getAnnotation(Version.class) != null) {
          versionPropertyName = propertyDescriptor.getName();
          versionPropertyType = propertyDescriptor.getPropertyType();
          finish++;
        }
        if (finish == 3) {
          break;
        }
      }
    }

    if (softDeletedPropertyName != null) {
      supportSoftDeleted = true;
      JpaExtProperties.SoftDelete softDeleteProperties = jpaExtProperties.getSoftDelete();
      PropertyDescriptor propertyDescriptor = BeanUtils
          .getPropertyDescriptor(domainClass, softDeletedPropertyName);
      softDeletedWriteMethod = Objects.requireNonNull(propertyDescriptor).getWriteMethod();
      softDeletedReadMethod = propertyDescriptor.getReadMethod();

      if (!"".equals(trueValue)) {
        this.softDeletedTrueValue = trueValue;
      } else {
        this.softDeletedTrueValue = softDeleteProperties.getTrueValue();
      }
      this.softDeletedTrueValue = JpaUtil.convert(this.softDeletedTrueValue,
          softDeletedPropertyType);
      if (!"".equals(falseValue)) {
        this.softDeletedFalseValue = falseValue;
      } else {
        this.softDeletedFalseValue = softDeleteProperties.getFalseValue();
      }
      this.softDeletedFalseValue = JpaUtil.convert(this.softDeletedFalseValue,
          softDeletedPropertyType);
    }
  }

  @Override
  public void setSoftDeleted(Object entity) {
    try {
      softDeletedWriteMethod.invoke(entity, softDeletedTrueValue);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void setUnSoftDeleted(Object entity) {
    try {
      softDeletedWriteMethod.invoke(entity, softDeletedFalseValue);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean isSoftDeleted(Object entity) {
    try {
      return softDeletedTrueValue.equals(softDeletedReadMethod.invoke(entity));
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean softDeletedSeted(Object entity) {
    try {
      return softDeletedReadMethod.invoke(entity) != null;
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean supportSoftDeleted() {
    return supportSoftDeleted;
  }

  @Override
  public Class<?> getSoftDeletedPropertyType() {
    return softDeletedPropertyType;
  }

  @Override
  public String getSoftDeletedPropertyName() {
    return softDeletedPropertyName;
  }

  @Override
  public Object getSoftDeletedTrueValue() {
    return softDeletedTrueValue;
  }

  @Override
  public Object getSoftDeletedFalseValue() {
    return softDeletedFalseValue;
  }

  @Override
  public String getLastModifiedDatePropertyName() {
    return this.lastModifiedDatePropertyName;
  }

  @Override
  public String getVersionPropertyName() {
    return this.versionPropertyName;
  }

  @Override
  public Object getLastModifiedDateNowValue() {
    return JpaUtil.convert(LocalDateTime.now(), this.lastModifiedDatePropertyType);
  }

  @Override
  public Object getVersionIncValue() {
    if (Date.class.isAssignableFrom(this.versionPropertyType)
        || TemporalAccessor.class.isAssignableFrom(this.versionPropertyType)) {
      return JpaUtil.convert(LocalDateTime.now(), this.versionPropertyType);
    } else {
      return JpaUtil.convert(1, this.versionPropertyType);
    }
  }
}
