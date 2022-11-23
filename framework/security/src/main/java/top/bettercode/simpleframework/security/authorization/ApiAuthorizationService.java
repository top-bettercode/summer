package top.bettercode.simpleframework.security.authorization;

import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;

public interface ApiAuthorizationService {

  void save(ApiAuthenticationToken authorization);

  void remove(ApiAuthenticationToken authorization);

  void remove(String scope, String username);

  default void validateUserDetails(UserDetails userDetails) {
  }

  @Nullable
  ApiAuthenticationToken findByScopeAndUsername(String scope, String username);

  @Nullable
  ApiAuthenticationToken findByAccessToken(String accessToken);

  @Nullable
  ApiAuthenticationToken findByRefreshToken(String refreshToken);

}
