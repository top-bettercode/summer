package top.bettercode.simpleframework.web.form;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 不支持 {@link FormDuplicateCheck} 自定义 expireSeconds
 *
 * @author Peter Wu
 */
public class FormkeyService implements IFormkeyService {

  private final ConcurrentMap<String, Long> nameCache;
  private final ConcurrentMap<Long, ConcurrentMap<String, Boolean>> caches;

  public FormkeyService(Long expireSeconds) {
    this.caches = new ConcurrentHashMap<>();
    Cache<String, Long> objectCache = Caffeine.newBuilder()
        .expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
    this.nameCache = objectCache.asMap();
  }

  private ConcurrentMap<String, Boolean> getCache(Long expireSeconds) {
    return caches.computeIfAbsent(expireSeconds, k -> new ConcurrentHashMap<>());
  }

  @Override
  public boolean exist(String formkey, long expireSeconds) {
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
