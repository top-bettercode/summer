package top.bettercode.summer.web.form;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author Peter Wu
 */
public class RedisFormkeyService implements IFormkeyService {

  private final RedisCacheWriter redisCacheWriter;
  private final String redisCacheName;
  private final Duration expireSeconds;

  public RedisFormkeyService(RedisConnectionFactory connectionFactory, String redisCacheName,
      long expireSeconds) {
    this.redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);
    this.redisCacheName = redisCacheName;
    this.expireSeconds = Duration.ofSeconds(expireSeconds);
  }


  @Override
  public boolean exist(String formkey, long expireSeconds) {
    return redisCacheWriter.putIfAbsent(redisCacheName, formkey.getBytes(StandardCharsets.UTF_8),
        new byte[]{1}, expireSeconds <= 0 ? this.expireSeconds : Duration.ofSeconds(expireSeconds))
        != null;
  }

  @Override
  public void remove(String formkey) {
    redisCacheWriter.remove(redisCacheName, formkey.getBytes(StandardCharsets.UTF_8));
  }

}
