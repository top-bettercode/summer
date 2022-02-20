package top.bettercode.simpleframework.support.code;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
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
    String type = propertiesSource.get(codeType + "|TYPE");
    Map<Serializable, String> codes = propertiesSource.mapOf(codeType, "Int".equals(type));
    for (Entry<Serializable, String> entry : codes.entrySet()) {
      if (name.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

  @Override
  public DicCodes getDicCodes(String codeType) {
    String type = propertiesSource.get(codeType + "|TYPE");
    Map<Serializable, String> codes = propertiesSource.mapOf(codeType, "Int".equals(type));
    return new DicCodes(codeType, propertiesSource.get(codeType), codes);
  }
}
