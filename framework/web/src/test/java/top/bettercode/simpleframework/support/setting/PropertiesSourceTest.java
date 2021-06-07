package top.bettercode.simpleframework.support.setting;

import top.bettercode.lang.property.PropertiesSource;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Wu
 */
class PropertiesSourceTest {

  @Test
  void mapOf() {
    PropertiesSource propertiesSource = new PropertiesSource("base-messages");
    Map<Object, Object> typeMismatch = propertiesSource.mapOf("typeMismatch");
    typeMismatch.forEach((k, v) -> System.err.println(k + ":" + v));
  }

  @Test
  void test() {
    PropertiesSource propertiesSource = new PropertiesSource("default-exception-handle");
    propertiesSource.all().forEach((k, v) -> System.err.println(k + ":" + v));
  }
}