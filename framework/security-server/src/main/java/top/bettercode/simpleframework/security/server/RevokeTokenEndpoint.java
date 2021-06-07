package top.bettercode.simpleframework.security.server;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Deprecated
@ConditionalOnWebApplication
@FrameworkEndpoint
public class RevokeTokenEndpoint {

  private final ConsumerTokenServices consumerTokenServices;
  private final IRevokeTokenService revokeTokenService;
  private final ResourceServerTokenServices resourceServerTokenServices;

  public RevokeTokenEndpoint(
      @Qualifier("consumerTokenServices") ConsumerTokenServices consumerTokenServices,
      @Autowired(required = false) IRevokeTokenService revokeTokenService,
      AuthorizationServerEndpointsConfiguration authorizationServerEndpointsConfiguration) {
    this.consumerTokenServices = consumerTokenServices;
    this.revokeTokenService = revokeTokenService;
    this.resourceServerTokenServices = authorizationServerEndpointsConfiguration
        .getEndpointsConfigurer().getResourceServerTokenServices();
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/oauth/token")
  @ResponseBody
  public Object revokeToken(HttpServletRequest request, String access_token) {
    if (revokeTokenService != null) {
      Authentication authResult = resourceServerTokenServices.loadAuthentication(access_token);
      Object principal = authResult.getPrincipal();
      revokeTokenService.revokeToken(principal, access_token);
    }
    consumerTokenServices.revokeToken(access_token);
    request.getSession().removeAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
    return ResponseEntity.noContent().build();
  }
}