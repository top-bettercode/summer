package top.bettercode.simpleframework.support.code;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import top.bettercode.lang.property.PropertiesSource;

/**
 * @author Peter Wu
 */
public class CodeService implements ICodeService {

  private final Logger log = LoggerFactory.getLogger(CodeService.class);
  private final PropertiesSource propertiesSource;

  public CodeService(PropertiesSource propertiesSource) {
    this.propertiesSource = propertiesSource;
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
    Assert.notNull(name, "name不能为空");
    Map<String, String> codes = propertiesSource.mapOf(codeType);
    Optional<String> first = codes.entrySet().stream()
        .filter(entry -> name.equals(entry.getValue())).map(Entry::getKey).findFirst();
    String code = first.orElse(null);
    if (code != null) {
      String type = propertiesSource.get(codeType + "|TYPE");
      boolean isInt = type == null ? isInt(code) : "Int".equals(type);
      if (isInt) {
        try {
          return Integer.parseInt(code);
        } catch (NumberFormatException e) {
          log.warn("状态码解析失败，期望Int类型的状态码");
          return code;
        }
      } else {
        return code;
      }
    } else {
      return null;
    }
  }

}
