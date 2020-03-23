package cn.bestwu.simpleframework.data.jpa.support;

import cn.bestwu.simpleframework.data.jpa.config.JpaExtProperties;
import cn.bestwu.simpleframework.data.jpa.SoftDelete;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.persistence.MappedSuperclass;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * @author Peter Wu
 */
public class DefaultSoftDeleteSupport implements SoftDeleteSupport {

  private static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();

  /**
   * 是否支持逻辑删除
   */
  private boolean support = false;
  /**
   * 逻辑删除字段属性名.
   */
  private String propertyName;
  /**
   * 默认逻辑删除值.
   */
  private Object trueValue;
  /**
   * 默认逻辑未删除值.
   */
  private Object falseValue;
  private Method readMethod;
  private Method writeMethod;
  private SimplePath<Object> path;


  public DefaultSoftDeleteSupport(JpaExtProperties jpaExtProperties, Class<?> domainClass)
      throws BeansException {
    this(jpaExtProperties, domainClass, null);
  }

  public <T> DefaultSoftDeleteSupport(JpaExtProperties jpaExtProperties, Class<?> domainClass,
      EntityPath<T> entityPath) throws BeansException {
    SoftDelete annotation = null;
    Class<?> descriptClass = domainClass;
    if (descriptClass.getSuperclass().isAnnotationPresent(MappedSuperclass.class)) {
      descriptClass = descriptClass.getSuperclass();
    }
    for (Field declaredField : descriptClass.getDeclaredFields()) {
      annotation = declaredField.getAnnotation(SoftDelete.class);
      if (annotation != null) {
        propertyName = declaredField.getName();
        break;
      }
    }
    if (annotation == null) {
      PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(descriptClass);
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
        annotation = propertyDescriptor.getReadMethod().getAnnotation(SoftDelete.class);
        if (annotation != null) {
          propertyName = propertyDescriptor.getName();
          break;
        }
      }
    }

    if (annotation != null) {
      support = true;
      JpaExtProperties.SoftDelete softDelete = jpaExtProperties.getSoftDelete();
      PropertyDescriptor propertyDescriptor = BeanUtils
          .getPropertyDescriptor(domainClass, propertyName);
      writeMethod = propertyDescriptor.getWriteMethod();
      readMethod = propertyDescriptor.getReadMethod();
      Class<?> propertyType = propertyDescriptor.getPropertyType();

      if (entityPath != null) {
        path = Expressions.path(propertyType, entityPath, propertyName);
      }

      String trueValue = annotation.trueValue();
      if (!"".equals(trueValue)) {
        this.trueValue = trueValue;
      } else {
        this.trueValue = softDelete.getTrueValue();
      }
      this.trueValue = CONVERSION_SERVICE.convert(this.trueValue, propertyType);
      String falseValue = annotation.falseValue();
      if (!"".equals(falseValue)) {
        this.falseValue = falseValue;
      } else {
        this.falseValue = softDelete.getFalseValue();
      }
      this.falseValue = CONVERSION_SERVICE.convert(this.falseValue, propertyType);
    }
  }

  @Override
  public void setSoftDeleted(Object entity) {
    try {
      writeMethod.invoke(entity, trueValue);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void setUnSoftDeleted(Object entity) {
    try {
      writeMethod.invoke(entity, falseValue);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean isSoftDeleted(Object entity) {
    try {
      return trueValue.equals(readMethod.invoke(entity));
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean support() {
    return support;
  }

  @Override
  public String getPropertyName() {
    return propertyName;
  }

  @Override
  public Object getTrueValue() {
    return trueValue;
  }

  @Override
  public Object getFalseValue() {
    return falseValue;
  }

  @Override
  public Predicate andTruePredicate(Predicate predicate) {
    return getPredicate(predicate, trueValue);
  }

  @Override
  public Predicate andFalsePredicate(Predicate predicate) {
    return getPredicate(predicate, falseValue);
  }

  protected Predicate getPredicate(Predicate predicate, Object value) {
    if (predicate == null) {
      return path.eq(value);
    } else {
      return path.eq(value).and(predicate);
    }
  }
}
