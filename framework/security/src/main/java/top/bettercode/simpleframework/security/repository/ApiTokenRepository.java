package top.bettercode.simpleframework.security.repository;

import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import top.bettercode.simpleframework.security.ApiToken;

public interface ApiTokenRepository {

  void save(ApiToken authorization);

  void remove(ApiToken authorization);

  void remove(String scope, String username);

  default void validateUserDetails(UserDetails userDetails) {
  }

  @Nullable
  ApiToken findByScopeAndUsername(String scope, String username);

  @Nullable
  ApiToken findByAccessToken(String accessToken);

  @Nullable
  ApiToken findByRefreshToken(String refreshToken);

}
