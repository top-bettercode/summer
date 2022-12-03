package top.bettercode.summer.web.support.code;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import top.bettercode.summer.tools.lang.property.PropertiesSource;

/**
 * @author Peter Wu
 */
public class CodeService implements ICodeService {

  private final PropertiesSource propertiesSource;

  private final long expireSeconds = 60L;
  private final Cache<String, DicCodes> cache = Caffeine.newBuilder()
      .expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();

  public CodeService(PropertiesSource propertiesSource) {
    this.propertiesSource = propertiesSource;
  }

  @Override
  public DicCodes getDicCodes(String codeType) {
    return cache.get(codeType,
        type -> new DicCodes(type, propertiesSource.get(type),
            propertiesSource.mapOf(type,
                "Int".equals(propertiesSource.get(type + "|TYPE")))));
  }
}
