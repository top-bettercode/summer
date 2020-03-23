package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.support.code.DicCodeType;
import cn.bestwu.simpleframework.support.code.DicCodeTypes;
import java.io.Serializable;

/**
 * @author Peter Wu
 */
public class StringCodeService implements ICodeService {

  private DicCodeTypes dicCodeTypes = new DicCodeTypes(false);

  public void setDicCodeTypes(DicCodeTypes dicCodeTypes) {
    this.dicCodeTypes = dicCodeTypes;
  }

  @Override
  public String getCode(String codeType, String name) {
    DicCodeType dicCodeType = dicCodeTypes.get(codeType);
    return dicCodeType == null ? null : (String) dicCodeType.getCodes().codeOf(name);
  }

  @Override
  public String getName(String codeType, Serializable code) {
    if (code instanceof Integer) {
      code = String.valueOf(code);
    }
    DicCodeType dicCodeType = dicCodeTypes.get(codeType);
    return dicCodeType == null ? null : dicCodeType.getCodes().nameOf(code);
  }
}
