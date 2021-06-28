package top.bettercode.simpleframework.web;

import java.util.Collections;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import top.bettercode.lang.property.PropertiesSource;
import top.bettercode.lang.property.Settings;

/**
 * @author Peter Wu
 */
@Endpoint(id = "settings")
public class SettingsEndpoint {

  @WriteOperation
  public Object write(@Selector String baseName, String key, String value) {
    PropertiesSource propertiesSource = Settings.INSTANCE.get(baseName);
    if (propertiesSource == null) {
      return Collections.emptyMap();
    } else {
      propertiesSource.put(key, value);
      return Collections.singletonMap(key, value);
    }
  }

  @DeleteOperation
  public Object delete(@Selector String baseName, String key) {
    PropertiesSource propertiesSource = Settings.INSTANCE.get(baseName);
    if (propertiesSource == null) {
      return Collections.emptyMap();
    } else {
      String remove = propertiesSource.remove(key);
      if (remove == null) {
        Map<String, String> map = propertiesSource.mapOf(key);
        for (String k : map.keySet()) {
          propertiesSource.remove(k);
        }
        return map;
      } else {
        return Collections.singletonMap(key, remove);
      }
    }
  }

  @ReadOperation
  public Object read(@Selector String baseName, @Nullable String key) {
    PropertiesSource propertiesSource = Settings.INSTANCE.get(baseName);
    if (propertiesSource == null) {
      return Collections.emptyMap();
    } else {
      if (StringUtils.hasText(key)) {
        String value = propertiesSource.get(key);
        if (value == null) {
          return propertiesSource.mapOf(key);
        } else {
          return Collections.singletonMap(key, value);
        }
      } else {
        return propertiesSource.all();
      }
    }
  }

}
