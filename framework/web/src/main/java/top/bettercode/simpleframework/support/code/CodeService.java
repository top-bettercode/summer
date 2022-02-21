package top.bettercode.simpleframework.support.code;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import top.bettercode.lang.property.PropertiesSource;

/**
 * @author Peter Wu
 */
public class CodeService implements ICodeService {

  private final PropertiesSource propertiesSource;
  private final ConcurrentMap<String, DicCodes> cache = new ConcurrentHashMap<>();

  public CodeService(PropertiesSource propertiesSource) {
    this.propertiesSource = propertiesSource;
  }

  @Override
  public DicCodes getDicCodes(String codeType) {
    return cache.computeIfAbsent(codeType,
        ctype -> new DicCodes(ctype, propertiesSource.get(ctype),
            propertiesSource.mapOf(ctype, "Int".equals(propertiesSource.get(ctype + "|TYPE")))));
  }
}
