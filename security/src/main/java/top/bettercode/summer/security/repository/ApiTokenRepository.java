package top.bettercode.summer.security.repository;

import org.springframework.lang.Nullable;
import top.bettercode.summer.security.token.ApiToken;

public interface ApiTokenRepository {

  void save(ApiToken authorization);

  void remove(ApiToken authorization);

  void remove(String scope, String username);

  @Nullable
  ApiToken findByScopeAndUsername(String scope, String username);

  @Nullable
  ApiToken findByAccessToken(String accessToken);

  @Nullable
  ApiToken findByRefreshToken(String refreshToken);

}
