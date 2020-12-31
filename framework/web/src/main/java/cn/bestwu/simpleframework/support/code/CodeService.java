package cn.bestwu.simpleframework.support.code;

import cn.bestwu.lang.property.PropertiesSource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Wu
 */
public class CodeService implements ICodeService {

  private final PropertiesSource propertiesSource;
  private final Map<String, Serializable> codeProperties;

  public CodeService(PropertiesSource propertiesSource) {
    this.propertiesSource = propertiesSource;
    codeProperties = new HashMap<>();
    propertiesSource.all().forEach((k, v) -> {
      String key = (String) k;
      String codeType;
      if (key.contains(".")) {
        String[] split = key.split("\\.");
        codeType = split[0];
        String code = split[1];
        if (v != null) {
          if (code.startsWith("0") && code.length() > 1) {
            codeProperties.put(codeType + "." + v, code);
          } else {
            try {
              int codeKey = Integer.parseInt(code);
              codeProperties.put(codeType + "." + v, codeKey);
            } catch (NumberFormatException e) {
              codeProperties.put(codeType + "." + v, code);
            }
          }
        }
      }
    });
  }

  @Override
  public String getName(String codeType) {
    return propertiesSource.getOrDefault(codeType, codeType);
  }

  @Override
  public String getName(String codeType, Serializable code) {
    return propertiesSource.getOrDefault(codeType + "." + code, String.valueOf(code));
  }

  @Override
  public Serializable getCode(String codeType, String name) {
    return codeProperties.get(codeType + "." + name);
  }

  @Override
  public DicCodes getDicCodes(String codeType) {
    Map<Object, Object> codes = propertiesSource.mapOf(codeType);
    codes.remove("");
    return new DicCodes(codeType, getName(codeType), codes);
  }
}
