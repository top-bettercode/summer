package top.bettercode.simpleframework.security.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.security.ClientDetailsProperties;

/**
 * @author Peter Wu
 */
public class AccessTokenService {

  private final UserDetailsService userDetailsService;
  private final OAuth2AuthorizationService oauth2AuthorizationService;
  private final AuthenticationManager authenticationManager;
  private final ClientDetailsProperties clientDetailsProperties;

  public AccessTokenService(
      UserDetailsService userDetailsService,
      OAuth2AuthorizationService oauth2AuthorizationService,
      AuthenticationManager authenticationManager,
      ClientDetailsProperties clientDetailsProperties) {
    this.userDetailsService = userDetailsService;
    this.oauth2AuthorizationService = oauth2AuthorizationService;
    this.authenticationManager = authenticationManager;
    this.clientDetailsProperties = clientDetailsProperties;
  }


  public OAuth2Authorization getOAuth2Authorization(UserDetails userDetails,
      Map<String, Object> requestParameters, String... scope) {
    if (requestParameters == null) {
      requestParameters = new HashMap<>();
    }
    Set<String> scopes;
    if (scope.length == 0) {
      scopes = clientDetailsProperties.getScope();
    } else {
      scopes = new HashSet<>(Arrays.asList(scope));
    }

    requestParameters.put("grant_type", "password");
    requestParameters.put("scope", StringUtils.collectionToCommaDelimitedString(scopes));
    requestParameters.put("username", userDetails.getUsername());
    requestParameters.put("password", userDetails.getPassword());

    OAuth2ClientAuthenticationToken authorizationGrantAuthentication = new OAuth2ClientAuthenticationToken(
        clientDetailsProperties.getClientId(), ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
        clientDetailsProperties.getClientSecret(),
        requestParameters);
    OAuth2ClientCredentialsAuthenticationToken clientCredentialsAuthenticationToken = new OAuth2ClientCredentialsAuthenticationToken(
        authorizationGrantAuthentication, scopes, requestParameters);

    OAuth2AccessTokenAuthenticationToken accessTokenAuthentication =
        (OAuth2AccessTokenAuthenticationToken) this.authenticationManager.authenticate(
            clientCredentialsAuthenticationToken);

    OAuth2AccessToken accessToken = accessTokenAuthentication.getAccessToken();
    return oauth2AuthorizationService.findByToken(accessToken.getTokenValue(),
        OAuth2TokenType.ACCESS_TOKEN);
  }

  public OAuth2Authorization getOAuth2Authorization(String username,
      Map<String, Object> requestParameters, String... scope) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return getOAuth2Authorization(userDetails, requestParameters, scope);
  }


  public OAuth2Authorization getOAuth2Authorization(String username,
      String... scope) {
    return getOAuth2Authorization(username, null, scope);
  }

  public void removeOAuth2Authorization(String username, String... scope) {
    OAuth2Authorization oauth2Authorization = getOAuth2Authorization(username,
        scope);
    oauth2AuthorizationService.remove(oauth2Authorization);
  }


}
