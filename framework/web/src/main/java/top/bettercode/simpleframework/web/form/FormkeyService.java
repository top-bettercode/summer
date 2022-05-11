package top.bettercode.simpleframework.web.form;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Wu
 */
public class FormkeyService implements IFormkeyService {

  private final ConcurrentMap<String, Boolean> cache;

  public FormkeyService(Long expireSeconds) {
    Cache<String, Boolean> objectCache = CacheBuilder
        .newBuilder().expireAfterWrite(expireSeconds, TimeUnit.SECONDS).maximumSize(1000).build();
    this.cache = objectCache.asMap();
  }


  @Override
  public boolean exist(String formkey) {
    return cache.putIfAbsent(formkey,true) != null;
  }

}
