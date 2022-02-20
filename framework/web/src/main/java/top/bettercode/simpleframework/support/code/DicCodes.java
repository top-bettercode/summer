package top.bettercode.simpleframework.support.code;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.util.Assert;

/**
 * @author Peter Wu
 */
public class DicCodes implements Serializable {

  private static final long serialVersionUID = 1975408514555403680L;


  private String type;

  private String name;

  private Map<Serializable, String> codes;

  //--------------------------------------------

  public DicCodes(String type, String name,
      Map<Serializable, String> codes) {
    this.type = type;
    this.name = name;
    this.codes = codes;
  }

  //--------------------------------------------
  public String getName(Serializable code) {
    return codes.getOrDefault(code, String.valueOf(code));
  }

  public Serializable getCode(String name) {
    Assert.notNull(name, "name不能为空");
    for (Entry<Serializable, String> entry : this.codes.entrySet()) {
      if (name.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

  //--------------------------------------------

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<Serializable, String> getCodes() {
    return codes;
  }

  public void setCodes(Map<Serializable, String> codes) {
    this.codes = codes;
  }
}
