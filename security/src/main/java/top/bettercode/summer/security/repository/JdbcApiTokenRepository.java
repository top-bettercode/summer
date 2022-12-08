package top.bettercode.summer.security.repository;

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
import top.bettercode.summer.security.token.ApiToken;

public class JdbcApiTokenRepository implements ApiTokenRepository {

  private final Logger log = LoggerFactory.getLogger(JdbcApiTokenRepository.class);

  private final String defaultInsertStatement;
  private final String defaultSelectStatement;
  private final String defaultSelectByAccessStatement;
  private final String defaultSelectByRefreshStatement;
  private final String defaultDeleteStatement;
  private final JdkSerializationSerializer jdkSerializationSerializer = new JdkSerializationSerializer();
  private final JdbcTemplate jdbcTemplate;

  public JdbcApiTokenRepository(DataSource dataSource) {
    this(dataSource, "api_token");
  }

  public JdbcApiTokenRepository(DataSource dataSource, String tableName) {
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
  public void save(ApiToken apiToken) {
    try {
      String scope = apiToken.getScope();
      String username = apiToken.getUsername();
      String id = scope + ":" + username;
      remove(scope, username);
      String accessToken = apiToken.getAccessToken().getTokenValue();
      String refreshToken = apiToken.getRefreshToken().getTokenValue();
      byte[] auth = jdkSerializationSerializer.serialize(apiToken);
      int update = jdbcTemplate.update(defaultInsertStatement,
          new Object[]{id, accessToken, refreshToken, new SqlLobValue(auth)},
          new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB});
      if (log.isDebugEnabled()) {
        log.debug("JdbcApiAuthorizationService.save\n{}\n{},{},{}\naffected:{}",
            defaultInsertStatement, id,
            accessToken, refreshToken, update);
      }
    } catch (DuplicateKeyException e) {
      save(apiToken);
    }
  }

  @Transactional
  @Override
  public void remove(ApiToken apiToken) {
    String scope = apiToken.getScope();
    String username = apiToken.getUsername();
    remove(scope, username);
  }

  @Transactional
  @Override
  public void remove(String scope, String username) {
    String id = scope + ":" + username;
    int update = jdbcTemplate.update(defaultDeleteStatement, id);
    if (log.isDebugEnabled()) {
      log.debug("JdbcApiAuthorizationService.remove\n{}\n{}\naffected:{}", defaultDeleteStatement,
          id, update);
    }
  }

  @Override
  public ApiToken findByScopeAndUsername(String scope, String username) {
    String id = scope + ":" + username;
    return getApiToken(id, defaultSelectStatement);
  }


  @Override
  public ApiToken findByAccessToken(String accessToken) {
    return getApiToken(accessToken, defaultSelectByAccessStatement);
  }

  @Override
  public ApiToken findByRefreshToken(String refreshToken) {
    return getApiToken(refreshToken, defaultSelectByRefreshStatement);
  }

  /**
   * @param param           参数，ID/token
   * @param selectStatement 查询语句
   * @return 结果
   */
  @Nullable
  private ApiToken getApiToken(String param, String selectStatement) {
    try {
      ApiToken apiToken = jdbcTemplate.queryForObject(selectStatement,
          (rs, rowNum) -> {
            byte[] bytes = rs.getBytes(1);
            if (JdkSerializationSerializer.isEmpty(bytes)) {
              return null;
            }
            try {
              return (ApiToken) jdkSerializationSerializer.deserialize(bytes);
            } catch (Exception e) {
              log.warn("apiToken反序列化失败", e);
              try {
                int update = jdbcTemplate.update(defaultDeleteStatement, rs.getString(2));
                if (log.isDebugEnabled()) {
                  log.debug(
                      "JdbcApiAuthorizationService.getApiAuthenticationToken delete\n{}\n{}\naffected:{}",
                      defaultDeleteStatement, rs.getString(2), update);
                }
              } catch (Exception ex) {
                log.warn("apiToken删除失败", ex);
              }
              return null;
            }
          }, param);
      if (log.isDebugEnabled()) {
        log.debug("JdbcApiAuthorizationService.getApiAuthenticationToken\n{}\n{}\nresult:{}",
            selectStatement, param, apiToken == null ? null : apiToken.getUserDetails());
      }
      return apiToken;
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

}
