package cn.bestwu.simpleframework.security.resource;

import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.CLIENT_ID;
import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.EXPIRES_AT;
import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.SCOPE;

import cn.bestwu.lang.util.LocalDateTimeHelper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * @author Peter Wu
 */
@Deprecated
public class SpringOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final ResourceServerTokenServices resourceServerTokenServices;

  public SpringOpaqueTokenIntrospector(
      AuthorizationServerEndpointsConfiguration authorizationServerEndpointsConfiguration) {
    this.resourceServerTokenServices = authorizationServerEndpointsConfiguration
        .getEndpointsConfigurer()
        .getResourceServerTokenServices();
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {
    OAuth2AccessToken oAuth2AccessToken = resourceServerTokenServices.readAccessToken(token);
    OAuth2Authentication oAuth2Authentication = resourceServerTokenServices
        .loadAuthentication(token);

    Map<String, Object> claims = new HashMap<>();

    OAuth2Request oAuth2Request = oAuth2Authentication.getOAuth2Request();
    if (oAuth2Request.getClientId() != null) {
      claims.put(CLIENT_ID, oAuth2Request.getClientId());
    }
    claims.put(EXPIRES_AT, LocalDateTimeHelper.of(oAuth2AccessToken.getExpiration()).toInstant());

    claims.put(SCOPE, oAuth2Request.getScope());

    return new DefaultOAuth2AuthenticatedPrincipal(claims, oAuth2Authentication.getAuthorities());
  }

}
