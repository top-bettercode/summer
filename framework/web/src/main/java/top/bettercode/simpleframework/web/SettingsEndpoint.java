package top.bettercode.simpleframework.web;

import java.util.Collections;
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
      return propertiesSource.all();
    }
  }

  @DeleteOperation
  public Object delete(@Selector String baseName, String key) {
    PropertiesSource propertiesSource = Settings.INSTANCE.get(baseName);
    if (propertiesSource == null) {
      return Collections.emptyMap();
    } else {
      propertiesSource.remove(key);
      return propertiesSource.all();
    }
  }

  @ReadOperation
  public Object read(@Selector String baseName, @Nullable String key) {
    PropertiesSource propertiesSource = Settings.INSTANCE.get(baseName);
    if (propertiesSource == null) {
      return Collections.emptyMap();
    } else {
      if (StringUtils.hasText(key)) {
        return propertiesSource.get(key);
      }
      return propertiesSource.all();
    }
  }

}
