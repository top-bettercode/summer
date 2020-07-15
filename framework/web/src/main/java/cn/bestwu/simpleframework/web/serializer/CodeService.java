package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.support.code.DicCodeType;
import cn.bestwu.simpleframework.support.code.DicCodeTypes;
import java.io.Serializable;

/**
 * @author Peter Wu
 */
public class CodeService implements ICodeService {

  private DicCodeTypes dicCodeTypes = new DicCodeTypes();

  public void setDicCodeTypes(DicCodeTypes dicCodeTypes) {
    this.dicCodeTypes = dicCodeTypes;
  }

  @Override
  public Integer getCode(String codeType, String name) {
    DicCodeType dicCodeType = dicCodeTypes.get(codeType);
    return dicCodeType == null ? null : (Integer) dicCodeType.getCodes().codeOf(name);
  }

  @Override
  public String getName(String codeType, Serializable code) {
    DicCodeType dicCodeType = dicCodeTypes.get(codeType);
    return dicCodeType == null ? null : dicCodeType.getCodes().nameOf(code);
  }
}
