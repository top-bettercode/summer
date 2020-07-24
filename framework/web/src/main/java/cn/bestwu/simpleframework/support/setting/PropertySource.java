package cn.bestwu.simpleframework.support.setting;

/**
 * @author Peter Wu
 */
public interface PropertySource {

  Object get(String key);

  void put(String key, Object value);

  void remove(String key);
}
