package cn.bestwu.simpleframework.support.code;

import cn.bestwu.lang.property.PropertiesSource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Wu
 */
public class CodeService implements ICodeService {

  private final Logger log = LoggerFactory.getLogger(CodeService.class);
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
          String type = propertiesSource.getString(codeType + "|TYPE");
          boolean isInt = type == null ? isInt(code) : "Int".equals(type);
          if (isInt) {
            try {
              int codeKey = Integer.parseInt(code);
              codeProperties.put(codeType + "." + v, codeKey);
            } catch (NumberFormatException e) {
              log.warn("状态码解析失败，期望Int类型的状态码");
              codeProperties.put(codeType + "." + v, code);
            }
          } else {
            codeProperties.put(codeType + "." + v, code);
          }
        }
      }
    });
  }

  private boolean isInt(String code) {
    if (code.startsWith("0") && code.length() > 1) {
      return false;
    } else {
      try {
        Integer.parseInt(code);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
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
