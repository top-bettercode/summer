package top.bettercode.summer.web.form;

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
  private final ConcurrentMap<Long, ConcurrentMap<String, Boolean>> caches;

  public FormkeyService(long expireSeconds) {
    Assert.isTrue(expireSeconds > 0, "过期时间必须大于0");
    this.expireSeconds = expireSeconds;
    this.caches = new ConcurrentHashMap<>();
  }

  private ConcurrentMap<String, Boolean> getCache(Long expireSeconds) {
    return caches.computeIfAbsent(expireSeconds, k -> {
      Cache<String, Boolean> objectCache = Caffeine.newBuilder()
          .expireAfterWrite(k, TimeUnit.SECONDS).build();
      return objectCache.asMap();
    });
  }

  @Override
  public boolean exist(String formkey, long expireSeconds) {
    expireSeconds = expireSeconds <= 0 ? this.expireSeconds : expireSeconds;
    Boolean present = getCache(expireSeconds).putIfAbsent(formkey, true);
    return present != null;
  }

  @Override
  public void remove(String formkey) {
    caches.values().forEach(map -> map.remove(formkey));
  }

}
