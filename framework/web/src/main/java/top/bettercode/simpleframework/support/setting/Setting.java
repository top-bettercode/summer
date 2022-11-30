package top.bettercode.simpleframework.support.setting;

import java.lang.reflect.Method;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import top.bettercode.lang.property.MapPropertySource;
import top.bettercode.lang.property.PropertySource;

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

  public static Setting of(Map<String, String> source) {
    return new Setting(new MapPropertySource(source));
  }

  public String get(String key) {
    return source.get(key);
  }

  public String getOrDefault(String key, String defaultValue) {
    return source.getOrDefault(key, defaultValue);
  }

  /**
   * 设置配置
   *
   * @param key   配置项
   * @param value 值
   */
  public void put(String key, String value) {
    source.put(key, value);
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
   * 根据ConfigurationProperties注解绑定配置
   *
   * @param target the target bindable
   * @param <T>    the bound type
   * @return the binding proxy result (never {@code null})
   */
  public <T> T bind(Class<T> target) {
    ConfigurationProperties annotation = target.getAnnotation(ConfigurationProperties.class);
    Assert.notNull(annotation, target.getName() + "无ConfigurationProperties注解");
    String prefix = annotation.value();
    if (!StringUtils.hasText(prefix)) {
      prefix = annotation.prefix();
    }
    return bind(prefix, target);
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
        return get(name, o, method, objects, methodProxy, methodName.substring(3));
      } else if (methodName.startsWith("is") && objects.length == 0) {
        return get(name, o, method, objects, methodProxy, methodName.substring(2));
      } else if (methodName.startsWith("set") && objects.length == 1) {
        Object result = methodProxy.invokeSuper(o, objects);
        String propertyName = StringUtils.uncapitalize(methodName.substring(3));
        String key = name + "." + propertyName;
        if (result != null) {
          String value = conversionService.convert(objects[0], String.class);
          put(key, value);
        } else {
          remove(key);
        }
        return result;
      } else {
        return methodProxy.invokeSuper(o, objects);
      }
    });
    @SuppressWarnings("unchecked")
    T t = (T) enhancer.create();
    return t;
  }

  private Object get(String name, Object o, Method method, Object[] objects,
      MethodProxy methodProxy, String propertyName) throws Throwable {
    propertyName = StringUtils.uncapitalize(propertyName);
    String key = name + "." + propertyName;
    Object result = get(key);
    if (result == null) {
      result = methodProxy.invokeSuper(o, objects);
      if (result != null) {
        put(key, conversionService.convert(result, String.class));
      }
    } else {
      result = conversionService.convert(result, method.getReturnType());
    }
    return result;
  }

}
