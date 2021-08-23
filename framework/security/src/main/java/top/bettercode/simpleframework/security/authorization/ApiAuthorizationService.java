package top.bettercode.simpleframework.security.authorization;

import org.springframework.lang.Nullable;
import top.bettercode.simpleframework.security.ApiAuthenticationToken;

public interface ApiAuthorizationService {

  void save(ApiAuthenticationToken authorization);

  void remove(ApiAuthenticationToken authorization);

  void remove(String id);

  @Nullable
  ApiAuthenticationToken findById(String id);

  ApiAuthenticationToken findByAccessToken(String accessToken);

  ApiAuthenticationToken findByRefreshToken(String refreshToken);

}
