package top.bettercode.summer.security.repository;

import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import top.bettercode.summer.security.ApiToken;
import top.bettercode.summer.security.config.ApiSecurityProperties;

public interface ApiTokenRepository {

  void save(ApiToken authorization);

  void remove(ApiToken authorization);

  void remove(String scope, String username);

  default void validateUserDetails(UserDetails userDetails) {
  }

  default boolean needKickedOut(ApiSecurityProperties securityProperties, String scope,
      UserDetails userDetails) {
    return securityProperties.needKickedOut(scope);
  }


  @Nullable
  ApiToken findByScopeAndUsername(String scope, String username);

  @Nullable
  ApiToken findByAccessToken(String accessToken);

  @Nullable
  ApiToken findByRefreshToken(String refreshToken);

}
