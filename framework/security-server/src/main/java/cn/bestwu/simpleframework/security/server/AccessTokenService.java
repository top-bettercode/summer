package cn.bestwu.simpleframework.security.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
@Deprecated
public class AccessTokenService {

  private final ClientDetails clientDetails;
  private final UserDetailsService userDetailsService;
  private final AuthorizationServerTokenServices authorizationServerTokenServices;
  private final TokenStore tokenStore;

  public AccessTokenService(ClientDetails clientDetails,
      UserDetailsService userDetailsService,
      AuthorizationServerTokenServices authorizationServerTokenServices,
      TokenStore tokenStore) {
    this.clientDetails = clientDetails;
    this.userDetailsService = userDetailsService;
    this.authorizationServerTokenServices = authorizationServerTokenServices;
    this.tokenStore = tokenStore;
  }

  public OAuth2AccessToken getAccessToken(UserDetails userDetails,
      Map<String, String> requestParameters, String... scope) {
    return getAccessToken(userDetails, requestParameters, new HashSet<>(Arrays.asList(scope)));
  }

  public OAuth2AccessToken getAccessToken(UserDetails userDetails,
      Map<String, String> requestParameters, Set<String> scope) {
    if (requestParameters == null) {
      requestParameters = new HashMap<>();
    }
    requestParameters.put("grant_type", "password");
    requestParameters.put("scope", StringUtils.collectionToCommaDelimitedString(scope));
    requestParameters.put("username", userDetails.getUsername());
    requestParameters.put("password", userDetails.getPassword());

    OAuth2Request request = new OAuth2Request(requestParameters, clientDetails.getClientId(), null,
        true, scope,
        null, null, null, null);
    OAuth2Authentication auth2Authentication = new OAuth2Authentication(request,
        new UsernamePasswordAuthenticationToken(userDetails, "N/A", userDetails.getAuthorities()));
    SecurityContextHolder.getContext().setAuthentication(auth2Authentication);
    return authorizationServerTokenServices.createAccessToken(auth2Authentication);
  }

  public OAuth2AccessToken getAccessToken(String username,
      Map<String, String> requestParameters, String... scope) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return getAccessToken(userDetails, requestParameters, scope);
  }

  public OAuth2AccessToken getAccessToken(String username,
      Map<String, String> requestParameters, Set<String> scope) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return getAccessToken(userDetails, requestParameters, scope);
  }

  public OAuth2AccessToken getAccessToken(String username, String... scope) {
    return getAccessToken(username, null, scope);
  }

  public OAuth2AccessToken getAccessToken(String username, Set<String> scope) {
    return getAccessToken(username, null, scope);
  }

  public OAuth2AccessToken getAccessToken(String username) {
    return getAccessToken(username, Collections.singleton("trust"));
  }

  public void removeAccessToken(String userName) {
    if (tokenStore != null) {
      for (OAuth2AccessToken oAuth2AccessToken : tokenStore
          .findTokensByClientIdAndUserName(clientDetails.getClientId(), userName)) {
        tokenStore.removeAccessToken(oAuth2AccessToken);
      }
    }
  }


}
