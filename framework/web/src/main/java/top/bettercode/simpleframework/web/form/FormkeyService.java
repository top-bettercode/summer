package top.bettercode.simpleframework.web.form;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.springframework.util.Assert;

/**
 * 不支持 {@link FormDuplicateCheck} 自定义 expireSeconds
 *
 * @author Peter Wu
 */
public class FormkeyService implements IFormkeyService {

  private final long expireSeconds;
  private final ConcurrentMap<String, Long> nameCache;
  private final ConcurrentMap<Long, ConcurrentMap<String, Boolean>> caches;

  public FormkeyService(long expireSeconds) {
    Assert.isTrue(expireSeconds > 0, "过期时间必须大于0");
    this.expireSeconds = expireSeconds;
    this.caches = new ConcurrentHashMap<>();
    Cache<String, Long> objectCache = Caffeine.newBuilder()
        .expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
    this.nameCache = objectCache.asMap();
  }

  private ConcurrentMap<String, Boolean> getCache(Long expireSeconds) {
    return caches.computeIfAbsent(expireSeconds, k -> {
      Cache<String, Boolean> objectCache = Caffeine.newBuilder()
          .expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
      return objectCache.asMap();
    });
  }

  @Override
  public boolean exist(String formkey, long expireSeconds) {
    expireSeconds = expireSeconds <= 0 ? this.expireSeconds : expireSeconds;
    Boolean present = getCache(expireSeconds).putIfAbsent(formkey, true);
    if (present == null) {
      nameCache.put(formkey, expireSeconds);
    }
    return present != null;
  }

  @Override
  public void remove(String formkey) {
    Long expireSeconds = nameCache.get(formkey);
    if (expireSeconds == null) {
      caches.values().forEach(map -> map.remove(formkey));
    } else {
      getCache(expireSeconds).remove(formkey);
    }
  }

}
