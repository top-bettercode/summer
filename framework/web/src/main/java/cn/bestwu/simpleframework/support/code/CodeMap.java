package cn.bestwu.simpleframework.support.code;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * 码表
 *
 * @author Peter Wu
 */
public class CodeMap extends LinkedHashMap<Serializable, String> implements ICodes {

  private static final long serialVersionUID = -2218418227017577000L;

  @Override
  public String nameOf(Serializable code) {
    return get(code);
  }

  @Override
  public Serializable codeOf(String name) {
    for (Entry<Serializable, String> entry : entrySet()) {
      if (name.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

}
