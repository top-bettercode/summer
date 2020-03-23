package cn.bestwu.simpleframework.support.code;

import java.io.Serializable;

/**
 * @author Peter Wu
 */
public class DicCodeType implements Serializable {

  private static final long serialVersionUID = 1975408514555403680L;

  private String type;

  private String name;

  private ICodes codes;

  //--------------------------------------------

  public DicCodeType(String type,String name) {
    this.type = type;
    this.name = name;
    this.codes = new CodeMap();
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

  public ICodes getCodes() {
    return codes;
  }

  public void setCodes(ICodes codes) {
    this.codes = codes;
  }
}
