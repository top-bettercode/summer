package top.bettercode.summer.security.repository;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import top.bettercode.summer.security.ApiToken;

public final class RedisApiTokenRepository implements ApiTokenRepository {

  private final Logger log = LoggerFactory.getLogger(RedisApiTokenRepository.class);
  private static final String API_AUTH = "api_auth:";
  private static final String ID = "id:";
  private static final String ACCESS_TOKEN = "access_token:";
  private static final String REFRESH_TOKEN = "refresh_token:";

  private final String keyPrefix;

  private static final boolean springDataRedis_2_0 = ClassUtils.isPresent(
      "org.springframework.data.redis.connection.RedisStandaloneConfiguration",
      RedisApiTokenRepository.class.getClassLoader());

  private final RedisConnectionFactory connectionFactory;
  private final JdkSerializationSerializer jdkSerializationSerializer = new JdkSerializationSerializer();

  private Method redisConnectionSet_2_0;

  public RedisApiTokenRepository(RedisConnectionFactory connectionFactory) {
    this(connectionFactory, "");
  }

  public RedisApiTokenRepository(RedisConnectionFactory connectionFactory, String prefix) {
    this.connectionFactory = connectionFactory;
    if (StringUtils.hasText(prefix)) {
      this.keyPrefix = API_AUTH + prefix + ":";
    } else {
      this.keyPrefix = API_AUTH;
    }
    if (springDataRedis_2_0) {
      this.loadRedisConnectionMethods_2_0();
    }
  }

  private void loadRedisConnectionMethods_2_0() {
    this.redisConnectionSet_2_0 = ReflectionUtils.findMethod(
        RedisConnection.class, "set", byte[].class, byte[].class);
  }

  private RedisConnection getConnection() {
    return connectionFactory.getConnection();
  }

  private byte[] serializeKey(String object) {
    return (this.keyPrefix + object).getBytes(StandardCharsets.UTF_8);
  }


  @Override
  public void save(ApiToken apiToken) {
    String scope = apiToken.getScope();
    String username = apiToken.getUsername();
    String id = scope + ":" + username;

    byte[] auth = jdkSerializationSerializer.serialize(apiToken);

    byte[] accessKey = serializeKey(ACCESS_TOKEN + apiToken.getAccessToken().getTokenValue());
    byte[] refreshKey = serializeKey(
        REFRESH_TOKEN + apiToken.getRefreshToken().getTokenValue());
    byte[] idKey = serializeKey(ID + id);

    try (RedisConnection conn = getConnection()) {
      ApiToken exist = getApiToken(idKey, conn);
      conn.openPipeline();
      //删除已存在
      if (exist != null) {
        byte[] existAccessKey = serializeKey(ACCESS_TOKEN + exist.getAccessToken().getTokenValue());
        byte[] existRefreshKey = serializeKey(
            REFRESH_TOKEN + exist.getRefreshToken().getTokenValue());
        if (!Arrays.equals(existAccessKey, accessKey)) {
          conn.keyCommands().del(existAccessKey);
        }
        if (!Arrays.equals(existRefreshKey, refreshKey)) {
          conn.keyCommands().del(existRefreshKey);
        }
      }

      if (springDataRedis_2_0) {
        try {
          this.redisConnectionSet_2_0.invoke(conn, accessKey, idKey);
          this.redisConnectionSet_2_0.invoke(conn, refreshKey, idKey);
          this.redisConnectionSet_2_0.invoke(conn, idKey, auth);
        } catch (Exception ex) {
          conn.closePipeline();
          throw new RuntimeException(ex);
        }
      } else {
        conn.stringCommands().set(accessKey, idKey);
        conn.stringCommands().set(refreshKey, idKey);
        conn.stringCommands().set(idKey, auth);
      }

      int access_expires_in = apiToken.getAccessToken().getExpires_in();
      int refresh_expires_in = apiToken.getRefreshToken().getExpires_in();
      conn.keyCommands().expire(accessKey, access_expires_in);
      conn.keyCommands().expire(refreshKey, refresh_expires_in);
      conn.keyCommands().expire(idKey, Math.max(access_expires_in, refresh_expires_in));

      conn.closePipeline();
    }
  }

  @Override
  public void remove(ApiToken apiToken) {
    String scope = apiToken.getScope();
    String username = apiToken.getUsername();
    String id = scope + ":" + username;
    byte[] accessKey = serializeKey(ACCESS_TOKEN + apiToken.getAccessToken().getTokenValue());
    byte[] refreshKey = serializeKey(
        REFRESH_TOKEN + apiToken.getRefreshToken().getTokenValue());
    byte[] idKey = serializeKey(ID + id);
    try (RedisConnection conn = getConnection()) {
      conn.openPipeline();
      conn.keyCommands().del(accessKey);
      conn.keyCommands().del(refreshKey);
      conn.keyCommands().del(idKey);
      conn.closePipeline();
    }
  }

  @Override
  public void remove(String scope, String username) {
    String id = scope + ":" + username;
    byte[] idKey = serializeKey(ID + id);
    try (RedisConnection conn = getConnection()) {
      ApiToken apiAuthenticationToken = getApiToken(idKey, conn);
      if (apiAuthenticationToken != null) {
        conn.openPipeline();
        byte[] accessKey = serializeKey(
            ACCESS_TOKEN + apiAuthenticationToken.getAccessToken().getTokenValue());
        byte[] refreshKey = serializeKey(
            REFRESH_TOKEN + apiAuthenticationToken.getRefreshToken().getTokenValue());
        conn.keyCommands().del(accessKey);
        conn.keyCommands().del(refreshKey);
        conn.keyCommands().del(idKey);
        conn.closePipeline();
      }
    }
  }

  @Override
  public ApiToken findByScopeAndUsername(String scope, String username) {
    String id = scope + ":" + username;
    byte[] idKey = serializeKey(ID + id);
    return findByIdKey(idKey);
  }

  @Nullable
  private ApiToken findByIdKey(byte[] idKey) {
    try (RedisConnection conn = getConnection()) {
      return getApiToken(idKey, conn);
    }
  }

  @Nullable
  private ApiToken getApiToken(byte[] idKey, RedisConnection conn) {
    byte[] bytes = conn.stringCommands().get(idKey);
    if (JdkSerializationSerializer.isEmpty(bytes)) {
      return null;
    }
    try {
      return (ApiToken) jdkSerializationSerializer.deserialize(bytes);
    } catch (Exception e) {
      log.warn("apiToken反序列化失败", e);
      try {
        conn.keyCommands().del(idKey);
      } catch (Exception ex) {
        log.warn("apiToken删除失败", ex);
      }
      return null;
    }
  }

  @Override
  public ApiToken findByAccessToken(String accessToken) {
    byte[] accessKey = serializeKey(ACCESS_TOKEN + accessToken);
    try (RedisConnection conn = getConnection()) {
      byte[] bytes = conn.stringCommands().get(accessKey);
      if (JdkSerializationSerializer.isEmpty(bytes)) {
        return null;
      }
      return getApiToken(bytes, conn);
    }
  }

  @Override
  public ApiToken findByRefreshToken(String refreshToken) {
    byte[] refreshKey = serializeKey(REFRESH_TOKEN + refreshToken);
    try (RedisConnection conn = getConnection()) {
      byte[] bytes = conn.stringCommands().get(refreshKey);
      if (JdkSerializationSerializer.isEmpty(bytes)) {
        return null;
      }
      return getApiToken(bytes, conn);
    }
  }
}
