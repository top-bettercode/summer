package cn.bestwu.simpleframework.support.code;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Peter Wu
 */
public class DicCodeTypes extends HashMap<String, DicCodeType> implements ICodeTypes {

  private static final long serialVersionUID = -2192651403471727025L;

  public DicCodeTypes() {
    try {
      ResourceBundle resourceBundle = ResourceBundle.getBundle("default-dic-code");
      init(resourceBundle);
    } catch (MissingResourceException ignored) {

    }
    try {
      ResourceBundle resourceBundle = ResourceBundle.getBundle("dic-code");
      init(resourceBundle);
    } catch (MissingResourceException ignored) {

    }
  }

  private void init(ResourceBundle resourceBundle) {
    Enumeration<String> keys = resourceBundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      String codeType;
      if (key.contains(".")) {
        String[] split = key.split("\\.");
        codeType = split[0];
        String code = split[1];
        DicCodeType dicCode = this.computeIfAbsent(codeType,
            k -> new DicCodeType(codeType, resourceBundle.getString(codeType)));
        try {
          int codeKey = Integer.parseInt(code);
          dicCode.getCodes().put(codeKey, resourceBundle.getString(key));
        } catch (NumberFormatException e) {
          dicCode.getCodes().put(code, resourceBundle.getString(key));
        }
      } else {
        codeType = key;
        this.computeIfAbsent(codeType,
            k -> new DicCodeType(codeType, resourceBundle.getString(codeType)));
      }
    }
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
