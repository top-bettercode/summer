package top.bettercode.environment;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import top.bettercode.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
@Endpoint(
    id = "config"
)
public class ConfigEndpoint {

  private final EnvironmentManager environment;

  public ConfigEndpoint(EnvironmentManager environment) {
    this.environment = environment;
  }

  @ReadOperation
  public Object write(@NonNull String ymlUrl) {
    try {
      Map<String, String> changed = new HashMap<>();
      List<PropertySource<?>> propertySources = new YamlPropertySourceLoader().load("remote",
          new UrlResource(ymlUrl));
      for (PropertySource<?> propertySource : propertySources) {
        MapPropertySource mapPropertySource = (MapPropertySource) propertySource;
        for (Entry<String, Object> entry : mapPropertySource.getSource().entrySet()) {
          Object value = entry.getValue();
          String key = entry.getKey();
          String val = value == null ? null : value.toString();
          boolean change = this.environment.setProperty(key, val);
          if (change) {
            changed.put(key, val);
          }
        }
      }
      return changed;
    } catch (IOException e) {
      return "配置文件加载错误：" + "\n" + StringUtil.valueOf(e);
    }
  }
}
