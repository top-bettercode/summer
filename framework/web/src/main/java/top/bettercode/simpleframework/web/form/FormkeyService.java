package top.bettercode.simpleframework.web.form;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Wu
 */
public class FormkeyService implements IFormkeyService {

  private final Cache<String, Boolean> cache;

  public FormkeyService(Long expireSeconds) {
    this.cache = CacheBuilder
        .newBuilder().expireAfterWrite(expireSeconds, TimeUnit.SECONDS).maximumSize(1000).build();
  }

  @Override
  public String putKey(String formkey) {
    cache.put(formkey, true);
    return formkey;
  }

  @Override
  public boolean exist(String formkey) {
    Boolean present = cache.getIfPresent(formkey);
    return present != null && present;
  }

}
