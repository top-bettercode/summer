package top.bettercode.simpleframework.security.authorization;

import java.sql.Types;
import javax.sql.DataSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.util.Assert;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;
import top.bettercode.simpleframework.security.config.ApiSecurityProperties;

public class JdbcApiAuthorizationService implements ApiAuthorizationService {

  private final Logger log = LoggerFactory.getLogger(JdbcApiAuthorizationService.class);

  private static final String DEFAULT_INSERT_STATEMENT = "insert into api_token (prefix, id, access_token, refresh_token, authentication) values (?, ?, ?, ?, ?)";

  private static final String DEFAULT_SELECT_STATEMENT = "select authentication from api_token where prefix=? and id = ?";

  private static final String DEFAULT_SELECT_BY_ACCESS_STATEMENT = "select authentication from api_token where prefix=? and access_token = ?";
  private static final String DEFAULT_SELECT_BY_REFRESH_STATEMENT = "select authentication from api_token where prefix=? and refresh_token = ?";

  private static final String DEFAULT_DELETE_STATEMENT = "delete from api_token where prefix=? and id = ?";

  private final String prefix;

  private final JdkSerializationSerializer jdkSerializationSerializer = new JdkSerializationSerializer();

  private final JdbcTemplate jdbcTemplate;

  public JdbcApiAuthorizationService(DataSource dataSource,
      ApiSecurityProperties securityProperties) {
    Assert.notNull(dataSource, "DataSource required");
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.prefix = securityProperties.getApiTokenSavePrefix();
  }

  @Override
  public void save(ApiAuthenticationToken authorization) {
    String id = authorization.getId();
    remove(id);
    String accessToken = authorization.getAccessToken().getTokenValue();
    String refreshToken = authorization.getRefreshToken().getTokenValue();
    byte[] auth = jdkSerializationSerializer.serialize(authorization);
    jdbcTemplate.update(DEFAULT_INSERT_STATEMENT,
        new Object[]{prefix, id, accessToken, refreshToken, new SqlLobValue(auth)}, new int[]{
            Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB});
  }

  @Override
  public void remove(ApiAuthenticationToken authorization) {
    String id = authorization.getId();
    jdbcTemplate.update(DEFAULT_DELETE_STATEMENT, prefix, id);
  }

  @Override
  public void remove(String id) {
    jdbcTemplate.update(DEFAULT_DELETE_STATEMENT, prefix, id);
  }

  @Override
  public ApiAuthenticationToken findById(String id) {
    return getApiAuthenticationToken(id, DEFAULT_SELECT_STATEMENT);
  }

  @Nullable
  private ApiAuthenticationToken getApiAuthenticationToken(String id, String selectStatement) {
    try {
      return jdbcTemplate.queryForObject(selectStatement,
          (rs, rowNum) -> {
            byte[] bytes = rs.getBytes(1);
            if (JdkSerializationSerializer.isEmpty(bytes)) {
              return null;
            }
            try {
              return (ApiAuthenticationToken) jdkSerializationSerializer.deserialize(bytes);
            } catch (Exception e) {
              log.error("apiToken反序列化失败", e);
              return null;
            }
          }, prefix, id);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  @Override
  public ApiAuthenticationToken findByAccessToken(String accessToken) {
    return getApiAuthenticationToken(accessToken, DEFAULT_SELECT_BY_ACCESS_STATEMENT);
  }

  @Override
  public ApiAuthenticationToken findByRefreshToken(String refreshToken) {
    return getApiAuthenticationToken(refreshToken, DEFAULT_SELECT_BY_REFRESH_STATEMENT);
  }


}
