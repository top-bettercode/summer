package top.bettercode.simpleframework.security.authorization;

import java.sql.Types;
import javax.sql.DataSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;

public class JdbcApiAuthorizationService implements ApiAuthorizationService {

  private final Logger log = LoggerFactory.getLogger(JdbcApiAuthorizationService.class);

  private final String defaultInsertStatement;
  private final String defaultSelectStatement;
  private final String defaultSelectByAccessStatement;
  private final String defaultSelectByRefreshStatement;
  private final String defaultDeleteStatement;
  private final JdkSerializationSerializer jdkSerializationSerializer = new JdkSerializationSerializer();
  private final JdbcTemplate jdbcTemplate;

  public JdbcApiAuthorizationService(DataSource dataSource) {
    this(dataSource, "api_token");
  }

  public JdbcApiAuthorizationService(DataSource dataSource, String tableName) {
    Assert.notNull(dataSource, "DataSource required");
    Assert.hasText(tableName, "tableName required");
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    defaultInsertStatement = "insert into " + tableName
        + " (id, access_token, refresh_token, authentication) values (?, ?, ?, ?)";
    defaultSelectStatement = "select authentication,id from " + tableName + " where id=?";
    defaultSelectByAccessStatement =
        "select authentication,id from " + tableName + " where access_token = ?";
    defaultSelectByRefreshStatement =
        "select authentication,id from " + tableName + " where refresh_token = ?";
    defaultDeleteStatement = "delete from " + tableName + " where id=?";
  }

  @Transactional
  @Override
  public void save(ApiAuthenticationToken authorization) {
    try {
      String scope = authorization.getScope();
      String username = authorization.getUsername();
      String id = scope + ":" + username;
      remove(scope, username);
      String accessToken = authorization.getAccessToken().getTokenValue();
      String refreshToken = authorization.getRefreshToken().getTokenValue();
      byte[] auth = jdkSerializationSerializer.serialize(authorization);
      jdbcTemplate.update(defaultInsertStatement,
          new Object[]{id, accessToken, refreshToken, new SqlLobValue(auth)},
          new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB});
    } catch (DuplicateKeyException e) {
      save(authorization);
    }
  }

  @Override
  public void remove(ApiAuthenticationToken authorization) {
    String scope = authorization.getScope();
    String username = authorization.getUsername();
    remove(scope, username);
  }

  @Override
  public void remove(String scope, String username) {
    String id = scope + ":" + username;
    jdbcTemplate.update(defaultDeleteStatement, id);
  }

  @Override
  public ApiAuthenticationToken findByScopeAndUsername(String scope, String username) {
    String id = scope + ":" + username;
    return getApiAuthenticationToken(id, defaultSelectStatement);
  }


  @Override
  public ApiAuthenticationToken findByAccessToken(String accessToken) {
    return getApiAuthenticationToken(accessToken, defaultSelectByAccessStatement);
  }

  @Override
  public ApiAuthenticationToken findByRefreshToken(String refreshToken) {
    return getApiAuthenticationToken(refreshToken, defaultSelectByRefreshStatement);
  }

  /**
   * @param param           参数，ID/token
   * @param selectStatement 查询语句
   * @return 结果
   */
  @Nullable
  private ApiAuthenticationToken getApiAuthenticationToken(String param, String selectStatement) {
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
              log.warn("apiToken反序列化失败", e);
              try {
                jdbcTemplate.update(defaultDeleteStatement, rs.getString(2));
              } catch (Exception ex) {
                log.warn("apiToken删除失败", ex);
              }
              return null;
            }
          }, param);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

}
