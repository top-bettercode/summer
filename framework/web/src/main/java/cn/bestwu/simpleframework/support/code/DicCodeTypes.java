package cn.bestwu.simpleframework.support.code;

import cn.bestwu.lang.property.PropertiesSource;
import java.util.HashMap;

/**
 * @author Peter Wu
 */
public class DicCodeTypes extends HashMap<String, DicCodeType> implements ICodeTypes {

  private static final long serialVersionUID = -2192651403471727025L;

  public DicCodeTypes() {
    PropertiesSource propertiesSource = new PropertiesSource("default-dic-code", "dic-code");
    propertiesSource.all().forEach((k, v) -> {
      String key = (String) k;
      String codeType;
      if (key.contains(".")) {
        String[] split = key.split("\\.");
        codeType = split[0];
        String code = split[1];
        DicCodeType dicCode = this.computeIfAbsent(codeType,
            type -> new DicCodeType(type, propertiesSource.getString(type)));
        String value = v == null ? null : String.valueOf(v);
        try {
          int codeKey = Integer.parseInt(code);
          dicCode.getCodes().put(codeKey, value);
        } catch (NumberFormatException e) {
          dicCode.getCodes().put(code, value);
        }
      } else {
        codeType = key;
        this.computeIfAbsent(codeType,
            type -> new DicCodeType(type, propertiesSource.getString(type)));
      }
    });

  }

  @Override
  public String nameOf(String codeType) {
    DicCodeType dicCode = get(codeType);
    return dicCode == null ? null : dicCode.getName();
  }

  @Override
  public ICodes codesOf(String codeType) {
    DicCodeType dicCode = get(codeType);
    return dicCode == null ? null : dicCode.getCodes();
  }
}
