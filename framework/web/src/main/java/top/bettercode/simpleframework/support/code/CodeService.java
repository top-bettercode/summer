package top.bettercode.simpleframework.support.code;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import top.bettercode.lang.property.PropertiesSource;

/**
 * @author Peter Wu
 */
public class CodeService implements ICodeService {

  private final PropertiesSource propertiesSource;

  private final long expireSeconds = 60L;
  private final Cache<String, DicCodes> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();

  public CodeService(PropertiesSource propertiesSource) {
    this.propertiesSource = propertiesSource;
  }

  @Override
  public DicCodes getDicCodes(String codeType) {
    try {
      return cache.get(codeType,
          () -> new DicCodes(codeType, propertiesSource.get(codeType),
              propertiesSource.mapOf(codeType,
                  "Int".equals(propertiesSource.get(codeType + "|TYPE")))));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
