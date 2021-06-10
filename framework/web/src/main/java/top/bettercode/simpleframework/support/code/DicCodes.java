package top.bettercode.simpleframework.support.code;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Peter Wu
 */
public class DicCodes implements Serializable {

  private static final long serialVersionUID = 1975408514555403680L;

  private String type;

  private String name;

  private Map<String, Object> codes;

  //--------------------------------------------

  public DicCodes(String type, String name, Map<String, Object> codes) {
    this.type = type;
    this.name = name;
    this.codes = codes;
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

  public Map<String, Object> getCodes() {
    return codes;
  }

  public void setCodes(Map<String, Object> codes) {
    this.codes = codes;
  }
}
