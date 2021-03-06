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

  private static final String DEFAULT_INSERT_STATEMENT = "insert into api_token (id, access_token, refresh_token, authentication) values (?, ?, ?, ?)";

  private static final String DEFAULT_SELECT_STATEMENT = "select authentication,id from api_token where id=?";

  private static final String DEFAULT_SELECT_BY_ACCESS_STATEMENT = "select authentication,id from api_token where access_token = ?";
  private static final String DEFAULT_SELECT_BY_REFRESH_STATEMENT = "select authentication,id from api_token where refresh_token = ?";

  private static final String DEFAULT_DELETE_STATEMENT = "delete from api_token where id=?";

  private final JdkSerializationSerializer jdkSerializationSerializer = new JdkSerializationSerializer();

  private final JdbcTemplate jdbcTemplate;

  public JdbcApiAuthorizationService(DataSource dataSource) {
    Assert.notNull(dataSource, "DataSource required");
    this.jdbcTemplate = new JdbcTemplate(dataSource);
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
      jdbcTemplate.update(DEFAULT_INSERT_STATEMENT,
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
    jdbcTemplate.update(DEFAULT_DELETE_STATEMENT, id);
  }

  @Override
  public ApiAuthenticationToken findByScopeAndUsername(String scope, String username) {
    String id = scope + ":" + username;
    return getApiAuthenticationToken(id, DEFAULT_SELECT_STATEMENT);
  }


  @Override
  public ApiAuthenticationToken findByAccessToken(String accessToken) {
    return getApiAuthenticationToken(accessToken, DEFAULT_SELECT_BY_ACCESS_STATEMENT);
  }

  @Override
  public ApiAuthenticationToken findByRefreshToken(String refreshToken) {
    return getApiAuthenticationToken(refreshToken, DEFAULT_SELECT_BY_REFRESH_STATEMENT);
  }

  /**
   * @param param           ?????????ID/token
   * @param selectStatement ????????????
   * @return ??????
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
              log.warn("apiToken??????????????????", e);
              try {
                jdbcTemplate.update(DEFAULT_DELETE_STATEMENT, rs.getString(2));
              } catch (Exception ex) {
                log.warn("apiToken????????????", ex);
              }
              return null;
            }
          }, param);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

}
