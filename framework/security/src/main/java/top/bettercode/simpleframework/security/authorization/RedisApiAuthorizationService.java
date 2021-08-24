package top.bettercode.simpleframework.security.authorization;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;

public final class RedisApiAuthorizationService implements ApiAuthorizationService {

  private final Logger log = LoggerFactory.getLogger(RedisApiAuthorizationService.class);
  private static final String API_AUTH = "api_auth:";
  private static final String ID = "id:";
  private static final String ACCESS_TOKEN = "access_token:";
  private static final String REFRESH_TOKEN = "refresh_token:";

  private final String keyPrefix;

  private static final boolean springDataRedis_2_0 = ClassUtils.isPresent(
      "org.springframework.data.redis.connection.RedisStandaloneConfiguration",
      RedisApiAuthorizationService.class.getClassLoader());

  private final RedisConnectionFactory connectionFactory;
  private final JdkSerializationSerializer jdkSerializationSerializer = new JdkSerializationSerializer();

  private Method redisConnectionSet_2_0;

  public RedisApiAuthorizationService(RedisConnectionFactory connectionFactory) {
    this(connectionFactory, "");
  }

  public RedisApiAuthorizationService(RedisConnectionFactory connectionFactory, String prefix) {
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
  public void save(ApiAuthenticationToken authorization) {
    String scope = authorization.getScope();
    String username = authorization.getUsername();
    String id = scope + ":" + username;

    remove(scope, username);

    byte[] auth = jdkSerializationSerializer.serialize(authorization);

    byte[] accessKey = serializeKey(ACCESS_TOKEN + authorization.getAccessToken().getTokenValue());
    byte[] refreshKey = serializeKey(
        REFRESH_TOKEN + authorization.getRefreshToken().getTokenValue());
    byte[] idKey = serializeKey(ID + id);

    try (RedisConnection conn = getConnection()) {
      conn.openPipeline();
      if (springDataRedis_2_0) {
        try {
          this.redisConnectionSet_2_0.invoke(conn, accessKey, idKey);
          this.redisConnectionSet_2_0.invoke(conn, refreshKey, idKey);
          this.redisConnectionSet_2_0.invoke(conn, idKey, auth);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      } else {
        conn.set(accessKey, idKey);
        conn.set(refreshKey, idKey);
        conn.set(idKey, auth);
      }

      int access_expires_in = authorization.getAccessToken().getExpires_in();
      int refresh_expires_in = authorization.getRefreshToken().getExpires_in();
      conn.expire(accessKey, access_expires_in);
      conn.expire(refreshKey, refresh_expires_in);
      conn.expire(idKey, Math.max(access_expires_in, refresh_expires_in));

      conn.closePipeline();
    }
  }

  @Override
  public void remove(ApiAuthenticationToken authorization) {
    String scope = authorization.getScope();
    String username = authorization.getUsername();
    String id = scope + ":" + username;
    byte[] accessKey = serializeKey(ACCESS_TOKEN + authorization.getAccessToken().getTokenValue());
    byte[] refreshKey = serializeKey(
        REFRESH_TOKEN + authorization.getRefreshToken().getTokenValue());
    byte[] idKey = serializeKey(ID + id);
    try (RedisConnection conn = getConnection()) {
      conn.openPipeline();
      conn.del(accessKey);
      conn.del(refreshKey);
      conn.del(idKey);
      conn.closePipeline();
    }
  }

  @Override
  public void remove(String scope, String username) {
    ApiAuthenticationToken authenticationToken = findByScopeAndUsername(scope, username);
    if (authenticationToken != null) {
      remove(authenticationToken);
    }
  }

  @Override
  public ApiAuthenticationToken findByScopeAndUsername(String scope, String username) {
    String id = scope + ":" + username;
    byte[] idKey = serializeKey(ID + id);
    return findByIdKey(idKey);
  }

  @Nullable
  private ApiAuthenticationToken findByIdKey(byte[] idKey) {
    try (RedisConnection conn = getConnection()) {
      byte[] bytes = conn.get(idKey);
      if (JdkSerializationSerializer.isEmpty(bytes)) {
        return null;
      }
      try {
        return (ApiAuthenticationToken) jdkSerializationSerializer.deserialize(bytes);
      } catch (Exception e) {
        log.error("apiToken反序列化失败", e);
      }
    }

    return null;
  }

  @Override
  public ApiAuthenticationToken findByAccessToken(String accessToken) {
    byte[] accessKey = serializeKey(ACCESS_TOKEN + accessToken);
    try (RedisConnection conn = getConnection()) {
      byte[] bytes = conn.get(accessKey);
      if (JdkSerializationSerializer.isEmpty(bytes)) {
        return null;
      }
      return findByIdKey(bytes);
    }
  }

  @Override
  public ApiAuthenticationToken findByRefreshToken(String refreshToken) {
    byte[] refreshKey = serializeKey(REFRESH_TOKEN + refreshToken);
    try (RedisConnection conn = getConnection()) {
      byte[] bytes = conn.get(refreshKey);
      if (JdkSerializationSerializer.isEmpty(bytes)) {
        return null;
      }
      return findByIdKey(bytes);
    }
  }
}
