package top.bettercode.simpleframework.web.form;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Wu
 */
public class FormKeyService implements IFormKeyService {

  private final Cache<String, Boolean> cache;

  public FormKeyService(Long expireSeconds) {
    this.cache = CacheBuilder
        .newBuilder().expireAfterWrite(expireSeconds, TimeUnit.SECONDS).maximumSize(1000).build();
  }

  @Override
  public String putKey(String formKey) {
    cache.put(formKey, true);
    return formKey;
  }

  @Override
  public boolean exist(String formKey) {
    Boolean present = cache.getIfPresent(formKey);
    return present != null && present;
  }

}
