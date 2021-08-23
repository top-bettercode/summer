package top.bettercode.simpleframework.security.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;

public final class RedisAuthorizationService implements ApiAuthorizationService {

  private final Logger log = LoggerFactory.getLogger(RedisAuthorizationService.class);
  private static final String API_AUTHORIZATION = "api_authorization:";
  private static final String ID = "id:";
  private static final String ACCESS_TOKEN = "access_token:";
  private static final String REFRESH_TOKEN = "refresh_token:";

  private final String prefix;

  private static final boolean springDataRedis_2_0 = ClassUtils.isPresent(
      "org.springframework.data.redis.connection.RedisStandaloneConfiguration",
      RedisAuthorizationService.class.getClassLoader());

  private final RedisConnectionFactory connectionFactory;
  private final ObjectMapper objectMapper;

  private Method redisConnectionSet_2_0;

  public RedisAuthorizationService(RedisConnectionFactory connectionFactory,
      ObjectMapper objectMapper, ApiSecurityProperties securityProperties) {
    this.connectionFactory = connectionFactory;
    this.objectMapper = objectMapper;
    this.prefix = API_AUTHORIZATION + securityProperties.getApiTokenSavePrefix() + ":";
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
    return (this.prefix + object).getBytes(StandardCharsets.UTF_8);
  }

  public static boolean isEmpty(byte[] bytes) {
    return bytes == null || bytes.length == 0;
  }


  @Override
  public void save(ApiAuthenticationToken authorization) {
    String id = authorization.getId();
    remove(id);

    byte[] auth;
    try {
      auth = objectMapper.writeValueAsBytes(authorization);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }

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
    String id = authorization.getId();
    byte[] accessKey = serializeKey(ACCESS_TOKEN + authorization.getAccessToken().getTokenValue());
    byte[] refreshKey = serializeKey(
        REFRESH_TOKEN + authorization.getRefreshToken().getTokenValue());
    byte[] idKey = serializeKey(ID + id);
    try (RedisConnection conn = getConnection()) {
      conn.openPipeline();
      conn.del(accessKey);
      conn.del(refreshKey);
      conn.del(idKey);
    }
  }

  @Override
  public void remove(String id) {
    ApiAuthenticationToken authenticationToken = findById(id);
    if (authenticationToken != null) {
      remove(authenticationToken);
    }
  }

  @Override
  public ApiAuthenticationToken findById(String id) {
    byte[] idKey = serializeKey(ID + id);
    try (RedisConnection conn = getConnection()) {
      byte[] bytes = conn.get(idKey);
      if (isEmpty(bytes)) {
        return null;
      }
      try {
        return objectMapper.readValue(bytes, ApiAuthenticationToken.class);
      } catch (IOException e) {
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
      if (isEmpty(bytes)) {
        return null;
      }
      String id = new String(bytes);
      if (id != null) {
        return findById(id);
      }
    }

    return null;
  }

  @Override
  public ApiAuthenticationToken findByRefreshToken(String refreshToken) {
    byte[] refreshKey = serializeKey(REFRESH_TOKEN + refreshToken);
    try (RedisConnection conn = getConnection()) {
      byte[] bytes = conn.get(refreshKey);
      if (isEmpty(bytes)) {
        return null;
      }
      String id = new String(bytes);
      if (id != null) {
        return findById(id);
      }
    }
    return null;
  }
}
