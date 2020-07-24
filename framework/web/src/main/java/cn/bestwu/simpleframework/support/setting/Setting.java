package cn.bestwu.simpleframework.support.setting;

import java.util.Map;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringUtils;

/**
 * 设置
 *
 * @author Peter Wu
 */
public class Setting {

  private final PropertySource source;
  private final ConversionService conversionService;

  private Setting(PropertySource source) {
    this.source = source;
    this.conversionService = ApplicationConversionService.getSharedInstance();
  }

  public static Setting of(PropertySource source) {
    return new Setting(source);
  }

  public static Setting of(Map<Object, Object> source) {
    return new Setting(new MapPropertySource(source));
  }

  public Object get(String key) {
    return source.get(key);
  }

  public Object getOrDefault(String key, Object defaultValue) {
    Object value = get(key);
    return value == null ? defaultValue : value;
  }

  /**
   * 设置配置
   *
   * @param key   配置项
   * @param value 值
   */
  public void put(String key, Object value) {
    if (value == null) {
      source.remove(key);
    } else {
      source.put(key, value);
    }
  }

  /**
   * 删除配置项
   *
   * @param key 配置项
   */
  public void remove(String key) {
    source.remove(key);
  }


  /**
   * 绑定配置
   *
   * @param name   the configuration property name to bind
   * @param target the target bindable
   * @param <T>    the bound type
   * @return the binding proxy result (never {@code null})
   */
  public <T> T bind(String name, Class<T> target) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(target);
    enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
      String methodName = method.getName();
      if (methodName.startsWith("get") && objects.length == 0) {
        String propertyName = StringUtils.uncapitalize(methodName.substring(3));
        Object result = get(name + "." + propertyName);
        if (result == null) {
          return methodProxy.invokeSuper(o, objects);
        } else {
          return conversionService.convert(result, method.getReturnType());
        }
      } else if (methodName.startsWith("is") && objects.length == 0) {
        String propertyName = StringUtils.uncapitalize(methodName.substring(2));
        Object result = get(name + "." + propertyName);
        if (result == null) {
          return methodProxy.invokeSuper(o, objects);
        } else {
          return conversionService.convert(result, method.getReturnType());
        }
      } else if (methodName.startsWith("set") && objects.length == 1) {
        Object result = methodProxy.invokeSuper(o, objects);
        String propertyName = StringUtils.uncapitalize(methodName.substring(3));
        put(name + "." + propertyName, objects[0]);
        return result;
      } else {
        return methodProxy.invokeSuper(o, objects);
      }
    });
    @SuppressWarnings("unchecked")
    T t = (T) enhancer.create();
    return t;
  }

}
