package cn.bestwu.simpleframework.support.setting;

import java.util.Map;

/**
 * @author Peter Wu
 */
public class MapPropertySource implements PropertySource {

  private final Map<Object, Object> source;

  public MapPropertySource(Map<Object, Object> source) {
    this.source = source;
  }

  @Override
  public Object get(String key) {
    return source.get(key);
  }

  @Override
  public void put(String key, Object value) {
    source.put(key, value);
  }

  @Override
  public void remove(String key) {
    source.remove(key);
  }
}
