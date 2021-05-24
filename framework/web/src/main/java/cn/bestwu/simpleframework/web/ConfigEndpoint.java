package cn.bestwu.simpleframework.web;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;

@Endpoint(id = "config")
public class ConfigEndpoint {

  private final ApplicationContext applicationContext;

  public ConfigEndpoint(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @ReadOperation
  public Map<String, Object> getConfig() {
    Map<String, Object> refreshScopeBeans = applicationContext
        .getBeansWithAnnotation(RefreshScope.class);
    return refreshScopeBeans.values().stream()
        .filter(o -> o.getClass().isAnnotationPresent(
            ConfigurationProperties.class))
        .collect(Collectors
            .toMap(o -> {
                  ConfigurationProperties annotation = o.getClass()
                      .getAnnotation(ConfigurationProperties.class);
                  String prefix = annotation.prefix();
                  return "".equals(prefix) ? annotation.value() : prefix;
                },
                o -> o));
  }

  @ReadOperation
  public Map<String, Object> getConfig(@Selector String toMatch) {
    Map<String, Object> result = getConfig();
    return result.entrySet().stream().filter(o -> o.getKey().startsWith(toMatch))
        .collect(Collectors.toMap(
            Entry::getKey, Entry::getValue));
  }
}